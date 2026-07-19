package io.alw.css.tradeconsumer.confirmation.repository;

import io.alw.css.confirmation.ConfirmationMatchEvent;
import io.alw.css.domain.common.CashflowConfirmationStatus;
import io.alw.css.domain.common.InputBy;
import io.alw.css.tradeconsumer.cashflow.model.jpa.RejectionEntity;
import io.alw.css.tradeconsumer.cashflow.repository.RejectionRepository;
import io.alw.css.tradeconsumer.confirmation.model.jpa.ConfirmationMatchStatusEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Types;
import java.util.List;

public class ConfirmationMatchStatusStore {
    private static final Logger log = LoggerFactory.getLogger(ConfirmationMatchStatusStore.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ConfirmationMatchStatusRepository confirmationMatchStatusRepository;
    private final RejectionRepository rejectionRepository;

    public ConfirmationMatchStatusStore(NamedParameterJdbcTemplate namedParameterJdbcTemplate, ConfirmationMatchStatusRepository confirmationMatchStatusRepository, RejectionRepository rejectionRepository) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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

    public void updateCashflowWithConfirmationStatus(String sql, ConfirmationMatchEvent confMatchEvent, CashflowConfirmationStatus confirmationStatus, InputBy inputBy, String inputByUserId) {
        long tradeId = confMatchEvent.tradeId();
        int tradeVersion = confMatchEvent.tradeVersion();
        long confReqId = confMatchEvent.matchRequestId();

        MapSqlParameterSource[] params = confMatchEvent
                .tradeLegMatchAttributes()
                .stream()
                .map(attr ->
                        new MapSqlParameterSource()
                                .addValue("p_trade_id", tradeId, Types.NUMERIC)
                                .addValue("p_trade_version", tradeVersion, Types.NUMERIC)
                                .addValue("p_trade_leg_id", attr.tradeLegId(), Types.NUMERIC)
                                .addValue("p_trade_leg_version", attr.tradeLegVersion(), Types.NUMERIC)
                                .addValue("p_conf_status", confirmationStatus.name(), Types.VARCHAR)
                                .addValue("p_conf_req_id", confReqId, Types.NUMERIC)
                                .addValue("p_input_by", inputBy.name(), Types.VARCHAR)
                                .addValue("p_input_by_user_id", inputByUserId, Types.VARCHAR)
                ).toArray(MapSqlParameterSource[]::new);

        int[] numOfRowsUpdated = namedParameterJdbcTemplate.batchUpdate(sql, params);
        int errCnt = 0;
        for (int num : numOfRowsUpdated) {
            if (num != 2) {
                ++errCnt;
            }
        }
        if (errCnt > 0) {
            throw new RuntimeException("Error updating cashflows with confirmation status. Num of updates with error: " + errCnt);
        }
    }
}
