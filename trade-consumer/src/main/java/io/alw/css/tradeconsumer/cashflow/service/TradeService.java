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
import io.alw.css.tradeconsumer.cashflow.mapper.TradeMapper;
import io.alw.css.tradeconsumer.cashflow.model.CashflowRejectionRecord;
import io.alw.css.tradeconsumer.cashflow.model.CashflowRejectionRecordBuilder;
import io.alw.css.tradeconsumer.cashflow.model.PreviousCashflowCheckOutcome;
import io.alw.css.tradeconsumer.cashflow.model.jpa.RejectionEntity;
import io.alw.css.tradeconsumer.cashflow.repository.CashflowStore;
import io.alw.css.tradeconsumer.confirmation.service.TradeConfirmationService;
import io.alw.css.tradeconsumer.model.CashflowSet;
import io.alw.css.tradeconsumer.model.constants.ExceptionServiceName;
import io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.alw.css.tradeconsumer.cashflow.model.PreviousCashflowCheckOutcome.*;

@Service
public class TradeService {
    private static final Logger log = LoggerFactory.getLogger(TradeService.class);
    private final TradeConfirmationService tradeConfirmationService;
    private final CashflowStore cashflowStore;
    private final CashflowVersionService cashflowVersionService;
    private final CashflowEnrichmentService cashflowEnrichmentService;
    private final TXRW txrw;

    public TradeService(TradeConfirmationService tradeConfirmationService, CashflowStore cashflowStore, CashflowVersionService cashflowVersionService, CashflowEnrichmentService cashflowEnrichmentService, TXRW txrw) {
        this.tradeConfirmationService = tradeConfirmationService;
        this.cashflowStore = cashflowStore;
        this.cashflowVersionService = cashflowVersionService;
        this.cashflowEnrichmentService = cashflowEnrichmentService;
        this.txrw = txrw;
    }

    public void process(TradeAvro tradeAvro, InputBy inputBy) {
        final String tradeType = tradeAvro.getTradeType();
        final Set<Cashflow> savedCashflows;
        final var groupedCashflows = new ArrayList<List<CashflowSet>>();

        try {
            // Map TradeAvro to one or more Cashflows
            var cashflowBuildersUnGrouped = TradeMapper.mapToDomain(tradeAvro, inputBy, inputBy.name());
            // Map TradeLinks
            var tradeLinkEntities = TradeMapper.mapTradeLinksToEntity(tradeAvro);

            // Group the cashflow builders for confirmation match request. Each group will be assigned a confMatchReqId
            List<List<CashflowBuilder>> groupedCashflowBuilders = tradeConfirmationService.groupCashflowsForConfirmationMatchRequest(Collections.unmodifiableList(cashflowBuildersUnGrouped), cashflowBuildersUnGrouped.getFirst().tradeType());

            var newCashflows = new HashSet<Cashflow>();
            var previousVersionCashflows = new HashSet<Cashflow>();
            for (List<CashflowBuilder> cashflowBuilders : groupedCashflowBuilders) {
                final var cashflowGroup = new ArrayList<CashflowSet>();

                // For each group of cashflowBuilders create a confirmation match request id
                // Note: confMatchReqId is generated eagerly(via DB sequence) prior to cashflow validation and enrichment which could reject the cashflow
                //       It is acceptable to lose confMatchReqId
                final long confMatchReqId = applyConfirmationEligibilityFunc();

                // Validate, Enrich and Build cashflows
                for (CashflowBuilder bdr : cashflowBuilders) {
                    // Set confMatchReqId for each cashflowBuilder
                    bdr.confReqId(confMatchReqId);
                    // Check for cases defined by `PreviousCashflowCheckOutcome`
                    PreviousCashflowCheckOutcome outcome = cashflowVersionService.checkAgainstPreviousVersionCashflow(bdr.tradeId(), bdr.tradeVersion(), bdr.tradeLegId(), bdr.tradeLegVersion());
                    // Validate and Enrich cashflow with nostroId, ssiId etc
                    CashflowSet cashflowSet = validateAndEnrichCashflow(outcome, bdr, newCashflows, previousVersionCashflows);
                    if (cashflowSet == null) {
                        log.warn("Result after validation and enrichment of the new cashflow to be processed is null. This will result in confirmation request failure or missing confirmation. TradeId-Ver: {}-{}, TradeLegId-Ver: {}-{}", bdr.tradeId(), bdr.tradeVersion(), bdr.tradeLegId(), bdr.tradeLegVersion());
                    }
                    cashflowGroup.add(cashflowSet);
                }

                groupedCashflows.add(cashflowGroup);
            }

            // Persist the cashflows and tradeLinks to database in a single transaction
            Supplier<Set<Cashflow>> persistAction = () -> {
                var savedCfs = cashflowStore.saveCashflows(newCashflows, previousVersionCashflows);
                if (tradeLinkEntities != null) {
                    cashflowStore.saveTradeLinks(tradeLinkEntities);
                }
                return savedCfs;
            };
            // Execute the DB transaction
            savedCashflows = txrw.execute(persistAction, Exception.class);

            var savedCfIds = savedCashflows.stream().map(cf -> cf.cashflowId() + "-" + cf.cashflowVersion()).collect(Collectors.joining(", "));
            log.info("Successfully processed cashflow for ALL trade legs. TradeType: {}, CashflowIds: {{}}", tradeType, savedCfIds);
        } catch (CategorizedRuntimeException e) {
            log.error("Failed to process trade. TradeType: {}. Msg: {}", tradeType, e.getMessage(), e);
            rejectCashflow(tradeAvro, e, inputBy);
            return;
        } catch (Exception e) {
            log.error("Failed to process trade. TradeType: {}. Msg: {}", tradeType, e.getMessage(), e);
            rejectCashflow(tradeAvro, CategorizedRuntimeException.UNKNOWN(e.getMessage(), tradeAvro), inputBy);
            return;
        }

        // Sent trade for matching
        tradeConfirmationService.sendForMatching(Collections.unmodifiableList(groupedCashflows));
    }

    private CashflowSet validateAndEnrichCashflow(PreviousCashflowCheckOutcome outcome, CashflowBuilder bdr, Set<Cashflow> newCashflows, Set<Cashflow> previousVersionCashflows) {
        return switch (outcome) {
            case InitialVersion _ -> {
                cashflowEnrichmentService.validateAndEnrich(bdr);

                CashflowSet.InitialVersion initialVersionCashflow = cashflowVersionService.createInitialVersionCashflow(bdr);
                newCashflows.add(initialVersionCashflow.cashflow());

                yield initialVersionCashflow;
            }
            case SubsequentVersion(var previousVersionCashflow) -> {
                cashflowEnrichmentService.validateAndEnrich(bdr);

                CashflowSet cashflowSet = cashflowVersionService.createSubsequentVersion(previousVersionCashflow, bdr);
                if (cashflowSet instanceof CashflowSet.SubsequentVersion subSeqSet) {
                    newCashflows.add(subSeqSet.revCashflow());
                    newCashflows.add(subSeqSet.amendCashflow());
                } else if (cashflowSet instanceof CashflowSet.CancelledVersion canSet) {
                    newCashflows.add(canSet.canCashflow());
                }
                previousVersionCashflows.add(previousVersionCashflow);

                yield cashflowSet;
            }
            case SameAsPrevCashflow _ -> {
                log.info("Received duplicate cashflow");
                yield null;
            }
            case PrevCashflowIsCancelled _ -> {
                ExceptionType exceptionType = ExceptionType.BUSINESS;
                ExceptionCategory exceptionCategory = ExceptionCategory.UNRECOVERABLE;
                String exceptionSubCategory = ExceptionSubCategoryType.LAST_CASHFLOW_IS_CANCELLED;
                String msg = "No further amendment is permitted when last cashflow is cancelled";
                boolean replayable = false;
                int numOfRetries = 0;
                LocalDateTime createdDateTime = LocalDateTime.now();
                InputBy inputBy = InputBy.CSS_TRD;

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

                yield null;
            }
        };
    }

    /// Ideally this method should check for confirmation eligibility and if eligible assign confirmation request id
    /// As of now, all cashflows are considered to be eligible and hence assigned a confirmation request id without any validations
    private long applyConfirmationEligibilityFunc() {
        return cashflowStore.getNewConfMatchReqId();
    }

    private void rejectCashflow(CashflowRejectionRecord rec) {
        CashflowBuilder bdr = rec.cashflowBdr();

        RejectionEntity cfr = new RejectionEntity();
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
                .setService(ExceptionServiceName.TRADE.value())
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

        try {
            txrw.executeWithoutResult(() -> cashflowStore.saveRejection(cfr), Exception.class);
        } catch (Exception e) {
            log.error("Failed to save cashflow rejection to database. TradeId-Ver: {}-{}", bdr.tradeId(), bdr.tradeVersion(), e);
            throw new RuntimeException(e);
        }
    }

    private void rejectCashflow(TradeAvro tradeAvro, CategorizedRuntimeException cre, InputBy inputBy) {
        RejectionEntity cfr = new RejectionEntity();
        cfr
                .setTradeId(tradeAvro.getTradeID())
                .setTradeVersion(tradeAvro.getTradeVersion())
                .setTradeType(tradeAvro.getTradeType())
                .setService(ExceptionServiceName.TRADE.value())
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

        try {
            txrw.executeWithoutResult(() -> cashflowStore.saveRejection(cfr), Exception.class);
        } catch (Exception e) {
            log.error("Failed to save cashflow rejection to database. TradeId-Ver: {}-{}", tradeAvro.getTradeID(), tradeAvro.getTradeVersion(), e);
            throw new RuntimeException(e);
        }
    }
}
