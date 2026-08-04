package io.alw.css.tradeconsumer.confirmation.service;

import io.alw.css.confirmation.ConfirmationMatchEvent;
import io.alw.css.dbshared.tx.TXRW;
import io.alw.css.domain.cashflow.Cashflow;
import io.alw.css.domain.cashflow.CashflowBuilder;
import io.alw.css.domain.common.*;
import io.alw.css.domain.exception.CategorizedRuntimeException;
import io.alw.css.domain.exception.ExceptionSubCategory;
import io.alw.css.serialization.confirmation.ConfirmationMatchEventAvro;
import io.alw.css.tradeconsumer.cashflow.model.jpa.RejectionEntity;
import io.alw.css.tradeconsumer.confirmation.ConfirmationMatchRequestPublisher;
import io.alw.css.tradeconsumer.confirmation.mapper.ConfirmationMatchEventMapper;
import io.alw.css.tradeconsumer.confirmation.model.ConfirmationMatchRequestFactoryOutcome;
import io.alw.css.tradeconsumer.confirmation.model.jpa.ConfirmationMatchStatusEntity;
import io.alw.css.tradeconsumer.confirmation.repository.ConfirmationMatchStatusStore;
import io.alw.css.tradeconsumer.confirmation.repository.sqlconstants.SqlConstants;
import io.alw.css.tradeconsumer.model.CashflowSet;
import io.alw.css.tradeconsumer.model.constants.ExceptionServiceName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

import static io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType.*;

@Service
public class TradeConfirmationService {
    private static final Logger log = LoggerFactory.getLogger(TradeConfirmationService.class);
    private final ConfirmationMatchRequestPublisher confirmationMatchRequestPublisher;
    private final ConfirmationMatchStatusStore confirmationMatchStatusStore;
    private final RandomGenerator rndm;
    private final TXRW txrw;

    public TradeConfirmationService(ConfirmationMatchRequestPublisher confirmationMatchRequestPublisher, ConfirmationMatchStatusStore confirmationMatchStatusStore, RandomGenerator rndm, TXRW txrw) {
        this.confirmationMatchRequestPublisher = confirmationMatchRequestPublisher;
        this.confirmationMatchStatusStore = confirmationMatchStatusStore;
        this.rndm = rndm;
        this.txrw = txrw;
    }

    public void process(ConfirmationMatchEventAvro avro, InputBy inputBy, String inputByUserId) {
        // Map avro to domain
        ConfirmationMatchEvent confMatchEvent = ConfirmationMatchEventMapper.instance().avroToDomain(avro);

        long tradeId = confMatchEvent.tradeId();
        int tradeVersion = confMatchEvent.tradeVersion();
        TradeType tradeType = confMatchEvent.tradeType();

        // Save confirmation match status
        List<ConfirmationMatchStatusEntity> entities = buildConfirmationMatchStatusEntities(confMatchEvent);
        try {
            txrw.executeWithoutResult(() -> confirmationMatchStatusStore.saveConfirmationMatchStatus(entities), Exception.class);
        } catch (Exception e) {
            log.error("Exception occurred when saving ConfirmationMatchEvent in database. TradeId-Ver: {}-{}, TradeType: {}. Exception: {}", tradeId, tradeVersion, tradeType, e.getMessage());
            var ex = CategorizedRuntimeException.TECHNICAL_RECOVERABLE("Exception occurred when saving ConfirmationMatchEvent in database",
                    new ExceptionSubCategory(CONF_STATUS_ENTRY_FAILURE, null));

            saveRejection(tradeId, tradeVersion, tradeType.name(), ex);
            throw ex;
        }

        // Corresponding to MatchStatus, confirm or un_confirm each cashflow
        // Determine cashflow confirmation status
        var confirmationStatus = switch (confMatchEvent.matchStatus()) {
            case MATCH, MANUAL_MATCH, MANUAL_FORCE_MATCH -> CashflowConfirmationStatus.CONFIRMED;
            case ALLEGED_MATCH, BREAK_MATCH, MANUAL_BREAK_MATCH -> CashflowConfirmationStatus.PENDING;
        };

        // Randomly select a sql statement to confirm or un-confirm cashflow
        // This is purely to check the performance from java layer
        final var cashflowConfirmationSql = rndm.nextBoolean()
                ? SqlConstants.UPDATE_CONFIRMATION_STATUS__MERGE_INTO
                : SqlConstants.UPDATE_CONFIRMATION_STATUS__ANONYMOUS_PROCEDURE;

        // Confirm or un-confirm all the cashflows atomically depending on the cashflow confirmation status
        try {
            int numOfCashflowsUpdated = txrw.execute(
                    () -> confirmationMatchStatusStore.updateCashflowWithConfirmationStatus(cashflowConfirmationSql, confMatchEvent, confirmationStatus, inputBy, inputByUserId),
                    Exception.class);

            log.info("Successfully updated cashflow confirmation status to {}. Number of cashflows: {} TradeType: {}, ConfirmationMatchRequestId: {}, ConfirmationMatchEventId-Ver: {}-{}",
                    confirmationStatus.name(), numOfCashflowsUpdated, confMatchEvent.tradeType().name(), confMatchEvent.matchRequestId(), confMatchEvent.eventId(), confMatchEvent.eventVersion());
        } catch (Exception e) {
            log.error("Exception occurred when confirming/un-confirming cashflows corresponding to the ConfirmationMatchEvent. TradeId-Ver: {}-{}, TradeType: {}. Exception: {}", tradeId, tradeVersion, tradeType, e.getMessage());
            var ex = CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("Exception occurred when confirming/un-confirming cashflows corresponding to the ConfirmationMatchEvent",
                    new ExceptionSubCategory(CASHFLOW_CONF_FAILURE, confMatchEvent));

            saveRejection(tradeId, tradeVersion, tradeType.name(), ex);
            throw ex;
        }
    }

    /// Returns an un modifiable list of cashflowBuilders grouped together corresponding to each confirmation match request
    public List<List<CashflowBuilder>> groupCashflowsForConfirmationMatchRequest(List<CashflowBuilder> cashflowBuilders, TradeType tradeType) {
        if (cashflowBuilders.isEmpty()) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("No trades to categorize for matching. The cashflows list is empty", new ExceptionSubCategory(EMPTY_CASHFLOW_LIST, null));
        }

        var groupedCashflowBuilders = switch (tradeType) {
            case FX -> TradeConfirmationServiceDelegate.groupForFx(cashflowBuilders);
            case MM_TERM -> TradeConfirmationServiceDelegate.groupForMmTerm(cashflowBuilders);
            case MM_CALL -> TradeConfirmationServiceDelegate.groupForMmCall(cashflowBuilders);
            case PAYMENT, FX_NDF, BOND, REPO, MM, OPTION -> throw new RuntimeException("No implementation yet for generating ConfirmationMatchRequest for TradeType: " + tradeType);
        };

        log.debug("Grouped cashflows for confirmation generation and matching. Number of groupings: {}", groupedCashflowBuilders.size());
        return Collections.unmodifiableList(groupedCashflowBuilders);
    }

    /// Sends the processed cashflows for matching with counterparty confirmation. Note: A confirmation message(ex: MT300) is not created by this CSS component.
    /// The given List of cashflows may produce multiple ConfirmationMatchRequests. Ex: for a list of Mm Cashflows
    public void sendForMatching(List<List<CashflowSet>> groupedCashflows) {
        if (groupedCashflows.isEmpty() || groupedCashflows.getFirst().isEmpty()) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("No cashflows present in the grouped cashflow list for which confirmation matching requests need to be generated", new ExceptionSubCategory(EMPTY_CASHFLOW_LIST, null));
        }

        // Get a cashflow with RevisionType != REV
        // ConfirmationMatchRequestFactory verifies whether all the cashflows belong to the same trade
        Cashflow referenceCashflow = groupedCashflows.getFirst().getFirst().primaryCashflow();
        long tradeId = referenceCashflow.tradeId();
        int tradeVersion = referenceCashflow.tradeVersion();
        TradeType tradeType = referenceCashflow.tradeType();

        log.info("Sending trade for confirmation generation and matching. Number of confirmation requests: {}", groupedCashflows.size());

        // Create ConfirmationMatchRequests
        List<ConfirmationMatchRequestFactoryOutcome> outcomes = TradeConfirmationServiceDelegate.buildConfirmationMatchRequests(groupedCashflows, tradeId, tradeVersion, tradeType);
        log.debug("Created confirmation match requests. Number of requests: {}", outcomes.size());

        // Save database audit
        try {
            txrw.executeWithoutResult(() -> saveConfMatchRequestAudit(outcomes), Exception.class);
        } catch (Exception e) {
            log.error("Exception occurred when saving ConfirmationMatchRequest audit entry in database. TradeId-Ver: {}-{}, TradeType: {}. Exception: {}", tradeId, tradeVersion, tradeType, e.getMessage());
            var ex = CategorizedRuntimeException.TECHNICAL_RECOVERABLE("Exception occurred when saving ConfirmationMatchRequest audit entry in database",
                    new ExceptionSubCategory(CONF_STATUS_ENTRY_FAILURE, null));

            saveRejection(outcomes, ex);
            throw ex;
        }

        // Publish for matching
        try {
            outcomes.forEach(o -> confirmationMatchRequestPublisher.publish(o.confMatchRequest()));
        } catch (Exception e) {
            log.error("Exception occurred when publishing ConfirmationMatchRequest to kafka topic. TradeId-Ver: {}-{}, TradeType: {}. Exception: {}", tradeId, tradeVersion, tradeType, e.getMessage());
            var ex = CategorizedRuntimeException.TECHNICAL_RECOVERABLE("Exception occurred when publishing ConfirmationMatchRequest to kafka topic",
                    new ExceptionSubCategory(CONF_REQ_PUB_FAILURE, null));

            saveRejection(outcomes, ex);
            throw ex;
        }
    }

    private void saveConfMatchRequestAudit(List<ConfirmationMatchRequestFactoryOutcome> outcomes) {
        List<ConfirmationMatchStatusEntity> jpaEntities = outcomes.stream()
                .map(ConfirmationMatchRequestFactoryOutcome::confMatchStatusJpaEntities)
                .flatMap(List::stream)
                .toList();
        confirmationMatchStatusStore.saveConfirmationMatchStatus(jpaEntities);
    }

    private List<ConfirmationMatchStatusEntity> buildConfirmationMatchStatusEntities(ConfirmationMatchEvent confMatchEvent) {
        return confMatchEvent
                .tradeLegMatchAttributes().stream()
                .map(tl -> {
                    var ent = new ConfirmationMatchStatusEntity();
                    ent.setConfRequestId(confMatchEvent.matchRequestId());
                    ent.setContraPairReqId(confMatchEvent.contraPairReqId());
                    ent.setTradeId(confMatchEvent.tradeId());
                    ent.setTradeVersion(confMatchEvent.tradeVersion());
                    ent.setTradeLegId(tl.tradeLegId());
                    ent.setTradeLegVersion(tl.tradeLegVersion());
                    ent.setMatchEventId(confMatchEvent.eventId());
                    ent.setMatchEventVersion(confMatchEvent.eventVersion());
                    ent.setNostroId(tl.nostroId());
                    ent.setSsiId(tl.ssiId());
                    ent.setSentOrRecd(SentOrRecd.RECD);
                    ent.setMatchStatus(confMatchEvent.matchStatus());
                    ent.setMatchDate(confMatchEvent.matchDate());
                    ent.setInputDateTime(LocalDateTime.now());

                    return ent;
                })
                .toList();
    }

    private void saveRejection(List<ConfirmationMatchRequestFactoryOutcome> outcomes, CategorizedRuntimeException cre) {
        Runnable action = () -> {
            for (ConfirmationMatchRequestFactoryOutcome outcome : outcomes) {
                var req = outcome.confMatchRequest();
                long tradeId = req.getTradeId();
                int tradeVersion = req.getTradeVersion();
                String tradeType = req.getTradeType();

                saveRejection(tradeId, tradeVersion, tradeType, cre);
            }
        };

        // execute db transaction
        txrw.executeWithoutResult(action, Exception.class);
    }

    private void saveRejection(long tradeId, int tradeVersion, String tradeType, CategorizedRuntimeException cre) {
        var ent = new RejectionEntity()
                .setTradeId(tradeId)
                .setTradeVersion(tradeVersion)
                .setTradeType(tradeType)
                //
                .setService(ExceptionServiceName.CONFIRMATION.value())
                .setExceptionType(cre.type().name())
                .setExceptionCategory(cre.category().name())
                .setExceptionSubCategory(cre.subCategory().type())
                .setMsg(cre.getMessage())
                .setReplayable(cre.replayable() ? YesNo.Y : YesNo.N)
                .setNumOfRetries(cre.numOfRetries())
                .setCreatedDateTime(cre.createdTime())
                .setInputBy(InputBy.CSS_CONF)
                .setUpdatedDateTime(LocalDateTime.now());

        confirmationMatchStatusStore.saveRejection(ent);
    }
}
