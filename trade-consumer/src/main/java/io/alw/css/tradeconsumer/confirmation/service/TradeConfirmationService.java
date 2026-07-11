package io.alw.css.tradeconsumer.confirmation.service;

import io.alw.css.confirmation.ConfirmationMatchStatus;
import io.alw.css.dbshared.tx.TXRW;
import io.alw.css.domain.cashflow.Cashflow;
import io.alw.css.domain.common.InputBy;
import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.common.YesNo;
import io.alw.css.domain.exception.CategorizedRuntimeException;
import io.alw.css.domain.exception.ExceptionSubCategory;
import io.alw.css.serialization.confirmation.ConfirmationMatchStatusAvro;
import io.alw.css.tradeconsumer.cashflow.model.jpa.RejectionEntity;
import io.alw.css.tradeconsumer.confirmation.ConfirmationMatchRequestPublisher;
import io.alw.css.tradeconsumer.confirmation.domain.ConfirmationMatchRequestFactory;
import io.alw.css.tradeconsumer.confirmation.mapper.ConfirmationMatchStatusMapper;
import io.alw.css.tradeconsumer.confirmation.model.ConfirmationMatchRequestFactoryOutcome;
import io.alw.css.tradeconsumer.confirmation.repository.ConfirmationMatchStatusStore;
import io.alw.css.tradeconsumer.model.constants.ExceptionServiceName;
import io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    public void process(ConfirmationMatchStatusAvro avro) {
        ConfirmationMatchStatus confMatchStatus = ConfirmationMatchStatusMapper.instance().avroToDomain(avro);

    }

    /// Sends the processed cashflows for matching with counterparty confirmation. Note: A confirmation message(ex: MT300) is not created by this CSS component.
    /// The given List of cashflows may produce multiple ConfirmationMatchRequests. Ex: for a list of Mm Cashflows
    public void sendForMatching(Set<Cashflow> cashflows) {
        Iterator<Cashflow> it = cashflows.iterator();
        final Cashflow cf;
        if (it.hasNext()) {
            cf = it.next();
        } else {
            log.warn("No trades to send for matching because the list of cashflows provided is empty");
            return;
        }
        long tradeId = cf.tradeId();
        int tradeVersion = cf.tradeVersion();
        TradeType tradeType = cf.tradeType();

        // Create ConfirmationMatchRequests
        List<ConfirmationMatchRequestFactoryOutcome> outcomes = switch (tradeType) {
            case FX -> ConfirmationMatchRequestFactory.forFx(cashflows, tradeId, tradeVersion, tradeType);
            case MM_TERM -> ConfirmationMatchRequestFactory.forMmTerm(cashflows, tradeId, tradeVersion, tradeType);
            case MM_CALL -> ConfirmationMatchRequestFactory.forMmCall(cashflows, tradeId, tradeVersion, tradeType);
            case PAYMENT, FX_NDF, BOND, REPO, MM, OPTION -> throw new RuntimeException("No implementation yet for generating ConfirmationMatchRequest for TradeType: " + tradeType);
        };

        // Save database audit
        txrw.executeWithoutResult(() -> saveConfMatchStatusAudit(outcomes), Exception.class);

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

    private void saveConfMatchStatusAudit(List<ConfirmationMatchRequestFactoryOutcome> outcomes) {
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
