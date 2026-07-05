package io.alw.css.tradeconsumer.cashflow.service;

import io.alw.css.dbshared.tx.TXRW;
import io.alw.css.domain.cashflow.Cashflow;
import io.alw.css.domain.cashflow.CashflowBuilder;
import io.alw.css.domain.common.InputBy;
import io.alw.css.domain.common.YesNo;
import io.alw.css.domain.exception.CategorizedRuntimeException;
import io.alw.css.domain.exception.ExceptionCategory;
import io.alw.css.domain.exception.ExceptionType;
import io.alw.css.serialization.trade.TradeAvro;
import io.alw.css.tradeconsumer.cashflow.model.CashflowRejectionRecord;
import io.alw.css.tradeconsumer.cashflow.model.CashflowRejectionRecordBuilder;
import io.alw.css.tradeconsumer.cashflow.model.jpa.CashflowEntity;
import io.alw.css.tradeconsumer.cashflow.model.jpa.CashflowRejectionEntity;
import io.alw.css.tradeconsumer.cashflow.processor.CashflowEnricher;
import io.alw.css.tradeconsumer.cashflow.processor.CashflowVersionManager;
import io.alw.css.tradeconsumer.cashflow.processor.PreviousCashflowCheckOutcome;
import io.alw.css.tradeconsumer.cashflow.processor.TradeMapper;
import io.alw.css.tradeconsumer.cashflow.repository.CashflowStore;
import io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.alw.css.tradeconsumer.cashflow.processor.PreviousCashflowCheckOutcome.*;

@Service
public class TradeService {
    private static final Logger log = LoggerFactory.getLogger(TradeService.class);
    private final CashflowStore cashflowStore;
    private final CashflowVersionManager cashflowVersionManager;
    private final CashflowEnricher cashflowEnricher;
    private final TXRW txrw;

    public TradeService(CashflowStore cashflowStore, CashflowVersionManager cashflowVersionManager, CashflowEnricher cashflowEnricher, TXRW txrw) {
        this.cashflowStore = cashflowStore;
        this.cashflowVersionManager = cashflowVersionManager;
        this.cashflowEnricher = cashflowEnricher;
        this.txrw = txrw;
    }

    public void process(TradeAvro tradeAvro, InputBy inputBy) {
        final long tradeID = tradeAvro.getTradeID();
        final int tradeVersion = tradeAvro.getTradeVersion();
        final int numOfTradeLegs = tradeAvro.getTradeLegs().size();
        log.info("Received TradeAvroMessage[tradeId: {}, tradeVersion: {}] with {} trade legs", tradeID, tradeVersion, numOfTradeLegs);

        try {
            // Map TradeAvro to one or more Cashflows
            var cashflowBuilders = TradeMapper.mapToDomain(tradeAvro, inputBy, inputBy.name());
            // Map TradeLinks
            var tradeLinkEntities = TradeMapper.mapTradeLinksToEntity(tradeAvro);

            List<Cashflow> newCashflows = new ArrayList<>();
            List<Cashflow> lastProcessedCashflows = new ArrayList<>();
            for (CashflowBuilder bdr : cashflowBuilders) {
                // Check for cases defined by `PreviousCashflowCheckOutcome`
                PreviousCashflowCheckOutcome outcome = cashflowVersionManager.checkAgainstLastProcessedCashflow(bdr.tradeId(), bdr.tradeVersion(), bdr.tradeLegId(), bdr.tradeLegVersion());
                // Validate and Enrich cashflows with nostroId, ssiId etc
                validateAndCreateCashflow(outcome, bdr, newCashflows, lastProcessedCashflows, tradeAvro);
            }

            // Persist the cashflows and tradeLinks to database in a single transaction
            Supplier<List<CashflowEntity>> persistAction = () -> {
                var savedCfs = cashflowStore.saveCashflows(newCashflows, lastProcessedCashflows);
                if (tradeLinkEntities != null) {
                    cashflowStore.saveTradeLinks(tradeLinkEntities);
                }
                return savedCfs;
            };
            // Execute the DB transaction
            var savedCashflows = txrw.execute(persistAction, Exception.class);

            var savedCfIds = savedCashflows.stream().map(cf -> cf.getCashflowEntityPK().cashflowId() + "-" + cf.getCashflowEntityPK().cashflowVersion()).collect(Collectors.joining(", "));
            log.info("Successfully processed cashflow for ALL trade legs. TradeType: {}, CashflowIds: {{}}", tradeAvro.getTradeType(), savedCfIds);
        } catch (CategorizedRuntimeException e) {
            log.info("Failed to process trade. TradeType: {}. Msg: {}", tradeAvro.getTradeType(), e.getMessage(), e);
            rejectCashflow(tradeAvro, e, inputBy);
        } catch (Exception e) {
            log.info("Failed to process trade. TradeType: {}. Msg: {}", tradeAvro.getTradeType(), e.getMessage(), e);
            rejectCashflow(tradeAvro, CategorizedRuntimeException.UNKNOWN(e.getMessage(), tradeAvro), inputBy);
        }
    }

    private void validateAndCreateCashflow(PreviousCashflowCheckOutcome outcome, CashflowBuilder bdr, List<Cashflow> newCashflows, List<Cashflow> lastProcessedCashflows, TradeAvro tradeAvro) {
        switch (outcome) {
            case InitialVersion _ -> {
                cashflowEnricher.validateAndEnrich(bdr);
                var cf = cashflowVersionManager.createInitialVersionCashflow(bdr);
                newCashflows.add(cf);
            }
            case SubsequentVersion(var lastProcessedCashflow) -> {
                cashflowEnricher.validateAndEnrich(bdr);
                List<Cashflow> cashflows = cashflowVersionManager.createSubsequentVersion(lastProcessedCashflow, bdr);
                newCashflows.addAll(cashflows);
                lastProcessedCashflows.add(lastProcessedCashflow);
            }
            case SameAsPrevCashflow _ -> {
                log.info("Received duplicate cashflow");
            }
            case PrevCashflowIsCancelled _ -> {
                ExceptionType exceptionType = ExceptionType.BUSINESS;
                ExceptionCategory exceptionCategory = ExceptionCategory.UNRECOVERABLE;
                String exceptionSubCategory = ExceptionSubCategoryType.LAST_CASHFLOW_IS_CANCELLED;
                String msg = "No further amendment is permitted when last cashflow is cancelled";
                boolean replayable = false;
                int numOfRetries = 0;
                LocalDateTime createdDateTime = LocalDateTime.now();
                InputBy inputBy = InputBy.CSS_SYS;

                log.info("Last cashflow is cancelled. No further amendment is permitted. TradeLegID-Ver: {}-{}", bdr.tradeLegId(), bdr.tradeLegVersion());
                rejectCashflow(CashflowRejectionRecordBuilder.builder()
                        .cashflowBdr(bdr)
                        .exceptionType(exceptionType)
                        .exceptionCategory(exceptionCategory)
                        .exceptionSubCategory(exceptionSubCategory)
                        .msg(msg)
                        .replayable(replayable)
                        .numOfRetries(numOfRetries)
                        .createdDateTime(createdDateTime)
                        .inputBy(inputBy)
                        .build());
            }
        }
    }

    private void rejectCashflow(CashflowRejectionRecord rec) {
        CashflowBuilder bdr = rec.cashflowBdr();

        try {
            CashflowRejectionEntity cfr = new CashflowRejectionEntity();
            cfr
                    .setTradeId(bdr.tradeId())
                    .setTradeVersion(bdr.tradeVersion())
                    .setTradeLegId(bdr.tradeLegId())
                    .setTradeLegVersion(bdr.tradeLegVersion())
                    .setTradeType(bdr.tradeType().name())
                    .setTradeLegType(bdr.tradeLegType().name())
                    .setValueDate(bdr.valueDate() == null ? null : bdr.valueDate())
                    .setEntityCode(bdr.entityCode())
                    .setCounterpartyCode(bdr.counterpartyCode())
                    .setAmount(bdr.amount())
                    .setCurrCode(bdr.currCode())
                    .setExceptionType(rec.exceptionType().name())
                    .setExceptionCategory(rec.exceptionCategory().name())
                    .setExceptionSubCategory(rec.exceptionSubCategory())
                    .setMsg(rec.msg())
                    .setReplayable(rec.replayable() ? YesNo.Y : YesNo.N)
                    .setNumOfRetries(rec.numOfRetries())
                    .setCreatedDateTime(rec.createdDateTime())
                    .setInputBy(rec.inputBy())
                    .setUpdatedDateTime(LocalDateTime.now())
            ;

            txrw.executeWithoutResult(() -> cashflowStore.saveRejection(cfr), Exception.class);
        } catch (Exception e) {
            log.error("Failed to save cashflow rejection to database. TradeId-Ver: {}-{}", bdr.tradeId(), bdr.tradeVersion(), e);
            throw new RuntimeException(e);
        }
    }

    private void rejectCashflow(TradeAvro tradeAvro, CategorizedRuntimeException cre, InputBy inputBy) {
        CashflowRejectionEntity cfr = new CashflowRejectionEntity();
        cfr
                .setTradeId(tradeAvro.getTradeID())
                .setTradeVersion(tradeAvro.getTradeVersion())
                .setTradeType(tradeAvro.getTradeType())
                .setExceptionType(cre.type().name())
                .setExceptionCategory(cre.category().name())
                .setExceptionSubCategory(cre.subCategory().type())
                .setMsg(cre.getMessage())
                .setReplayable(cre.replayable() ? YesNo.Y : YesNo.N)
                .setNumOfRetries(cre.numOfRetries())
                .setCreatedDateTime(cre.createdTime())
                .setInputBy(inputBy)
                .setUpdatedDateTime(LocalDateTime.now())
        ;
    }
}
