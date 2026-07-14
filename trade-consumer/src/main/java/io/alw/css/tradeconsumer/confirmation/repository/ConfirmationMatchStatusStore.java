package io.alw.css.tradeconsumer.confirmation.repository;

import io.alw.css.tradeconsumer.cashflow.model.jpa.RejectionEntity;
import io.alw.css.tradeconsumer.cashflow.repository.RejectionRepository;
import io.alw.css.tradeconsumer.confirmation.model.jpa.ConfirmationMatchStatusEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ConfirmationMatchStatusStore {
    private static final Logger log = LoggerFactory.getLogger(ConfirmationMatchStatusStore.class);
    private final ConfirmationMatchStatusRepository confirmationMatchStatusRepository;
    private final RejectionRepository rejectionRepository;

    public ConfirmationMatchStatusStore(ConfirmationMatchStatusRepository confirmationMatchStatusRepository, RejectionRepository rejectionRepository) {
        this.confirmationMatchStatusRepository = confirmationMatchStatusRepository;
        this.rejectionRepository = rejectionRepository;
    }


    public void saveConfirmationMatchStatus(List<ConfirmationMatchStatusEntity> entities) {
        for (ConfirmationMatchStatusEntity ent : entities) {
            log.trace("Saving ConfirmationMatchStatus to DB. Sent_or_Recd:{}, TradeId-Ver: {}-{}, TradeLegId-Ver: {}-{}, MatchEventId-Ver: {}-{}", ent.getSentOrRecd(), ent.getTradeId(), ent.getTradeVersion(), ent.getTradeLegId(), ent.getTradeLegVersion(), ent.getMatchEventId(), ent.getMatchEventVersion());
            confirmationMatchStatusRepository.save(ent);
        }
    }

    public void saveRejection(RejectionEntity ent) {
        rejectionRepository.save(ent);
    }
}
