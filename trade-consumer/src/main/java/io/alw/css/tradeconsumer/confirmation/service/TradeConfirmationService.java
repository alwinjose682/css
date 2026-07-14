package io.alw.css.tradeconsumer.confirmation.service;

import io.alw.css.confirmation.ConfirmationMatchEvent;
import io.alw.css.dbshared.tx.TXRW;
import io.alw.css.domain.cashflow.Cashflow;
import io.alw.css.domain.cashflow.CashflowBuilder;
import io.alw.css.domain.common.InputBy;
import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.common.YesNo;
import io.alw.css.domain.exception.CategorizedRuntimeException;
import io.alw.css.domain.exception.ExceptionSubCategory;
import io.alw.css.serialization.confirmation.ConfirmationMatchEventAvro;
import io.alw.css.tradeconsumer.cashflow.model.jpa.RejectionEntity;
import io.alw.css.tradeconsumer.confirmation.ConfirmationMatchRequestPublisher;
import io.alw.css.tradeconsumer.confirmation.mapper.ConfirmationMatchEventMapper;
import io.alw.css.tradeconsumer.confirmation.model.ConfirmationMatchRequestFactoryOutcome;
import io.alw.css.tradeconsumer.confirmation.repository.ConfirmationMatchStatusStore;
import io.alw.css.tradeconsumer.model.CashflowSet;
import io.alw.css.tradeconsumer.model.constants.ExceptionServiceName;
import io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType.EMPTY_CASHFLOW_LIST;

@Service
public class TradeConfirmationService {
    private static final Logger log = LoggerFactory.getLogger(TradeConfirmationService.class);
    private final ConfirmationMatchRequestPublisher confirmationMatchRequestPublisher;
    private final ConfirmationMatchStatusStore confirmationMatchStatusStore;
    private final TXRW txrw;

    public TradeConfirmationService(ConfirmationMatchRequestPublisher confirmationMatchRequestPublisher, ConfirmationMatchStatusStore confirmationMatchStatusStore, TXRW txrw) {
        this.confirmationMatchRequestPublisher = confirmationMatchRequestPublisher;
        this.confirmationMatchStatusStore = confirmationMatchStatusStore;
        this.txrw = txrw;
    }

    public void process(ConfirmationMatchEventAvro avro) {
        ConfirmationMatchEvent confMatchEvent = ConfirmationMatchEventMapper.instance().avroToDomain(avro);
        // TODO
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

        // Create ConfirmationMatchRequests
        List<ConfirmationMatchRequestFactoryOutcome> outcomes = TradeConfirmationServiceDelegate.buildConfirmationMatchRequests(groupedCashflows, tradeId, tradeVersion, tradeType);

        // Save database audit
        txrw.executeWithoutResult(() -> saveConfMatchRequestAudit(outcomes), Exception.class);

        // Publish for matching
        try {
            outcomes.forEach(o -> confirmationMatchRequestPublisher.publish(o.confMatchRequest()));
        } catch (Exception e) {
            log.error("Exception occurred when publishing ConfirmationMatchRequest to kafka topic. TradeId-Ver: {}-{}, TradeType: {}", tradeId, tradeVersion, tradeType);
            saveRejection(outcomes,
                    CategorizedRuntimeException.TECHNICAL_RECOVERABLE("Exception occurred when publishing ConfirmationMatchRequest to kafka topic",
                            new ExceptionSubCategory(ExceptionSubCategoryType.CONF_REQ_PUB_FAILURE, null))
            );
        }
    }

    private void saveConfMatchRequestAudit(List<ConfirmationMatchRequestFactoryOutcome> outcomes) {
        for (ConfirmationMatchRequestFactoryOutcome outcome : outcomes) {
            confirmationMatchStatusStore.saveConfirmationMatchStatus(outcome.confMatchStatusJpaEntities());
        }
    }

    private void saveRejection(List<ConfirmationMatchRequestFactoryOutcome> outcomes, CategorizedRuntimeException cre) {
        Runnable action = () -> {
            for (ConfirmationMatchRequestFactoryOutcome outcome : outcomes) {
                var req = outcome.confMatchRequest();
                long tradeId = req.getTradeId();
                int tradeVersion = req.getTradeVersion();
                for (var attr : req.getTradeLegMatchAttributes()) {
                    long tradeLegId = attr.getTradeLegId();
                    int tradeLegVersion = attr.getTradeLegVersion();
                    LocalDate valueDate = attr.getValueDate();

                    var ent = new RejectionEntity()
                            .setTradeId(tradeId)
                            .setTradeVersion(tradeVersion)
                            .setTradeLegId(tradeLegId)
                            .setTradeLegVersion(tradeLegVersion)
                            .setValueDate(valueDate)
                            //
                            .setService(ExceptionServiceName.CONFIRMATION.value())
                            .setExceptionType(cre.type().name())
                            .setExceptionCategory(cre.category().name())
                            .setExceptionSubCategory(cre.subCategory().type())
                            .setMsg(cre.getMessage())
                            .setReplayable(cre.replayable() ? YesNo.Y : YesNo.N)
                            .setNumOfRetries(cre.numOfRetries())
                            .setCreatedDateTime(cre.createdTime())
                            .setInputBy(InputBy.CSS_SYS)
                            .setUpdatedDateTime(LocalDateTime.now());

                    confirmationMatchStatusStore.saveRejection(ent);
                }
            }
        };

        // execute db transaction
        txrw.executeWithoutResult(action, Exception.class);
    }
}
