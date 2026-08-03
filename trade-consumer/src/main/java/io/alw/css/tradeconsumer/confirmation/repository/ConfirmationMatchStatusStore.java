package io.alw.css.tradeconsumer.confirmation.repository;

import io.alw.css.confirmation.ConfirmationMatchEvent;
import io.alw.css.domain.common.CashflowConfirmationStatus;
import io.alw.css.domain.common.InputBy;
import io.alw.css.tradeconsumer.cashflow.model.jpa.RejectionEntity;
import io.alw.css.tradeconsumer.cashflow.repository.RejectionRepository;
import io.alw.css.tradeconsumer.confirmation.model.jpa.ConfirmationMatchStatusEntity;
import io.alw.css.tradeconsumer.confirmation.repository.sqlconstants.SqlAndMetadata;
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
        confirmationMatchStatusRepository.saveAll(entities);
    }

    public void saveRejection(RejectionEntity ent) {
        rejectionRepository.save(ent);
    }

    public int updateCashflowWithConfirmationStatus(SqlAndMetadata sqlAndMetadata, ConfirmationMatchEvent confMatchEvent, CashflowConfirmationStatus confirmationStatus, InputBy inputBy, String inputByUserId) {
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

        int[] numOfRowsUpdated = namedParameterJdbcTemplate.batchUpdate(sqlAndMetadata.sql(), params);
        for (int i = 0; i < numOfRowsUpdated.length; ++i) {
            int updateNum = numOfRowsUpdated[i];
            if (updateNum != sqlAndMetadata.numberOfRecordsUpdated()) {
                MapSqlParameterSource param = params[i];
                long pConfReqId = (long) param.getValue("p_conf_req_id");
                long pTradeId = (long) param.getValue("p_trade_id");
                int pTradeVersion = (int) param.getValue("p_trade_version");
                long pTradeLegId = (long) param.getValue("p_trade_leg_id");
                int pTradeLegVersion = (int) param.getValue("p_trade_leg_version");

                log.error("Error updating cashflow with new confirmation status. Number of expected updates: {}, Actual updates: {}, ConfRequestId: {}, TradeId-Ver: {}-{}, TradeLegId-Ver: {}-{}",
                        sqlAndMetadata.numberOfRecordsUpdated(), updateNum, pConfReqId, pTradeId, pTradeVersion, pTradeLegId, pTradeLegVersion);
            }
        }

        return numOfRowsUpdated.length;
    }
}
