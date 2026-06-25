package io.alw.css.cashflowconsumer.service;

import io.alw.css.cashflowconsumer.model.CashflowRejectionRecord;
import io.alw.css.cashflowconsumer.model.constants.ExceptionSubCategoryType;
import io.alw.css.cashflowconsumer.model.jpa.CashflowRejectionEntity;
import io.alw.css.cashflowconsumer.processor.CashflowEnricher;
import io.alw.css.cashflowconsumer.processor.CashflowVersionManager;
import io.alw.css.cashflowconsumer.processor.PreviousCashflowCheckOutcome;
import io.alw.css.cashflowconsumer.processor.TradeMapper;
import io.alw.css.cashflowconsumer.repository.CashflowStore;
import io.alw.css.cashflowconsumer.util.DateUtil;
import io.alw.css.dbshared.tx.TXRW;
import io.alw.css.domain.cashflow.Cashflow;
import io.alw.css.domain.cashflow.CashflowBuilder;
import io.alw.css.domain.common.InputBy;
import io.alw.css.domain.common.YesNo;
import io.alw.css.domain.exception.CategorizedRuntimeException;
import io.alw.css.domain.exception.ExceptionCategory;
import io.alw.css.domain.exception.ExceptionType;
import io.alw.css.serialization.trade.TradeAvro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.alw.css.cashflowconsumer.processor.PreviousCashflowCheckOutcome.*;

@Service
public class CashflowService {
    private static final Logger log = LoggerFactory.getLogger(CashflowService.class);
    private final CashflowStore cashflowStore;
    private final CashflowVersionManager cashflowVersionManager;
    private final CashflowEnricher cashflowEnricher;
    private final TXRW txrw;

    public CashflowService(CashflowStore cashflowStore, CashflowVersionManager cashflowVersionManager, CashflowEnricher cashflowEnricher, TXRW txrw) {
        this.cashflowStore = cashflowStore;
        this.cashflowVersionManager = cashflowVersionManager;
        this.cashflowEnricher = cashflowEnricher;
        this.txrw = txrw;
    }

    public void process(TradeAvro tradeAvro, InputBy inputBy) {
        long tradeID = tradeAvro.getTradeID();
        int tradeVersion = tradeAvro.getTradeVersion();
        int numOfTradeLegs = tradeAvro.getTradeLegs().size();

        log.info("Received FoCashMessage[tradeId: {}, tradeVersion: {}] with {} trade legs", tradeID, tradeVersion, numOfTradeLegs);
        try {
            List<CashflowBuilder> cashflowBuilders = TradeMapper.mapToDomain(tradeAvro, inputBy, inputBy.name());

            List<Cashflow> newCashflows = new ArrayList<>();
            List<Cashflow> lastProcessedCashflows = new ArrayList<>();
            for (CashflowBuilder bdr : cashflowBuilders) {
                PreviousCashflowCheckOutcome outcome = cashflowVersionManager.checkAgainstLastProcessedCashflow(bdr.tradeId(), bdr.tradeVersion(), bdr.tradeLegId(), bdr.tradeLegVersion());
                validateAndCreateCashflow(outcome, bdr, newCashflows, lastProcessedCashflows, tradeAvro);
            }

            txrw.executeWithoutResult(_ -> cashflowStore.saveCashflows(newCashflows, lastProcessedCashflows));
            var cfIds = newCashflows.stream().map(cf -> cf.cashflowId() + "-" + cf.cashflowVersion()).collect(Collectors.joining(", "));
            log.info("Successfully created and processed cashflow for ALL trade legs. CashflowIds: {{}}", cfIds);
        } catch (CategorizedRuntimeException e) {
            log.info("Failed to process trade: {}-{}. Msg: {}", tradeID, tradeVersion, e.getMessage(), e);
            rejectCashflow(tradeAvro, e, inputBy);
        } catch (Exception e) {
            log.info("Failed to process trade: {}-{}. Msg: {}", tradeID, tradeVersion, e.getMessage(), e);
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
                rejectCashflow(tradeAvro, exceptionType, exceptionCategory, exceptionSubCategory, msg, replayable, numOfRetries, createdDateTime, inputBy);
            }
        }
    }

    private void rejectCashflow(TradeAvro foMsg, CategorizedRuntimeException cre, InputBy inputBy) {
        rejectCashflow(foMsg, cre.type(), cre.category(), cre.subCategory().type(), cre.getMessage(), cre.replayable(), cre.numOfRetries(), cre.createdTime(), inputBy);
    }

    private void rejectCashflow(CashflowRejectionRecord rec) {

        try {
            CashflowRejectionEntity cfr = new CashflowRejectionEntity();
            cfr
                    .setTradeID(foMsg.getTradeID())
                    .setTradeVersion(foMsg.getTradeVersion())

                    .setTradeType(foMsg.getTradeType())
                    .setValueDate(foMsg.getValueDate() == null ? null : DateUtil.formatValueDate(foMsg.getValueDate()))
                    .setEntityCode(foMsg.getEntityCode())
                    .setCounterpartyCode(foMsg.getCounterpartyCode())
                    .setAmount(foMsg.getAmount())
                    .setCurrCode(foMsg.getCurrCode())
                    .setExceptionType(exceptionType.name())
                    .setExceptionCategory(exceptionCategory.name())
                    .setExceptionSubCategory(exceptionSubCategory)
                    .setMsg(msg)
                    .setReplayable(replayable ? YesNo.Y : YesNo.N)
                    .setNumOfRetries(numOfRetries)
                    .setCreatedDateTime(createdDateTime)
                    .setInputBy(inputBy)
                    .setUpdatedDateTime(LocalDateTime.now())
            ;

            txrw.executeWithoutResult(_ -> cashflowStore.saveRejection(cfr));
        } catch (Exception e) {
            log.error("Failed to save cashflow rejection to database. FoCashflowID-Ver: {}-{}", tradeLegId, tradeLegVersion, e);
            throw new RuntimeException(e);
        }
    }
}
