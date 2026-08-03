package io.alw.css.tradeconsumer.confirmation.repository.sqlconstants;

public final class SqlConstants {
    private static final String UPDATE_CONFIRMATION_STATUS__MERGE_INTO_SQL = """
            MERGE INTO cashflow X USING(
                select A.*, M.action from (
                    select cf.rowid as row_id, cf.*
                    from cashflow cf
                    where trade_id = :p_trade_id and trade_version = :p_trade_version and trade_leg_id = :p_trade_leg_id and trade_leg_version = :p_trade_leg_version and latest = 'Y'
                    --and confirmation_status <> :p_conf_status
                    and ROWNUM = 1
                ) A
                CROSS JOIN(
                    select 'UPDATE' as action from dual
                	UNION ALL
                	select 'INSERT' as action from dual
                ) M
            ) Y
            ON (X.rowid = Y.row_id and action = 'UPDATE')
            WHEN MATCHED THEN
            update set X.latest = 'N'
            WHEN NOT MATCHED THEN
            insert(
            X.CASHFLOW_ID, X.CASHFLOW_VERSION, X.LATEST, X.REVISION_TYPE, X.TRADE_ID, X.TRADE_VERSION, X.TRADE_LEG_ID, X.TRADE_LEG_VERSION,
            X.TRADE_TYPE, X.TRADE_LEG_TYPE, X.BOOK_CODE, X.COUNTER_BOOK_CODE, X.TRANSACTION_TYPE, X.RATE, X.VALUE_DATE, X.ENTITY_CODE,
            X.COUNTERPARTY_CODE, X.AMOUNT, X.CURR_CODE, X.INTERNAL, X.NOSTRO_ID, X.SSI_ID, X.CONFIRMATION_STATUS, X.CONF_REQ_ID,
            X.PAYMENT_SUPPRESSION_CATEGORY, X.INPUT_BY, X.INPUT_BY_USER_ID, X.INPUT_DATE_TIME
            )
            values(
            Y.CASHFLOW_ID,
            Y.CASHFLOW_VERSION + 1,
            'Y',
            Y.REVISION_TYPE, -- No change to revision_type. Unlike cashflow amendment, no new record with RevisionType.REV is created
            Y.TRADE_ID, Y.TRADE_VERSION, Y.TRADE_LEG_ID, Y.TRADE_LEG_VERSION, Y.TRADE_TYPE, Y.TRADE_LEG_TYPE, Y.BOOK_CODE, Y.COUNTER_BOOK_CODE, Y.TRANSACTION_TYPE, Y.RATE, Y.VALUE_DATE, Y.ENTITY_CODE, Y.COUNTERPARTY_CODE, Y.AMOUNT, Y.CURR_CODE, Y.INTERNAL,
            Y.NOSTRO_ID, Y.SSI_ID,
            :p_conf_status,
            :p_conf_req_id,
            Y.PAYMENT_SUPPRESSION_CATEGORY,
            :p_input_by,
            :p_input_by_user_id,
            SYSTIMESTAMP
            )
            """;

    private static final String UPDATE_CONFIRMATION_STATUS__ANONYMOUS_PROCEDURE_SQL = """
            DECLARE
              v_cashflow_row cashflow%ROWTYPE;
              v_update_count NUMBER;
            BEGIN
            
              -- Fetch the latest row into a variable
              select * into v_cashflow_row
              from cashflow
              where trade_id = :p_trade_id and trade_version = :p_trade_version
                and trade_leg_id = :p_trade_leg_id and trade_leg_version = :p_trade_leg_version
                and latest = 'Y';
            
              -- Update latest cashflow record to non-latest
              update cashflow
              set latest = 'N'
              where trade_id = :p_trade_id and trade_version = :p_trade_version
                and trade_leg_id = :p_trade_leg_id and trade_leg_version = :p_trade_leg_version
                and latest = 'Y';
            
              v_update_count := SQL%ROWCOUNT;
            
              -- Validate exactly 1 cashflow is updated
              IF v_update_count <> 1 THEN
                RAISE_APPLICATION_ERROR(-20001, 'cashflow.latest update failed');
              END IF;
            
              -- Update confirmation_status
              v_cashflow_row.latest := 'Y';
              v_cashflow_row.cashflow_version := v_cashflow_row.cashflow_version + 1;
              v_cashflow_row.confirmation_status := :p_conf_status;
              v_cashflow_row.conf_req_id := :p_conf_req_id;
              v_cashflow_row.input_by := :p_input_by;
              v_cashflow_row.input_by_user_id := :p_input_by_user_id;
              v_cashflow_row.input_date_time := SYSTIMESTAMP; -- := SYSDATE
            
                -- Insert the updated row
              insert into cashflow values v_cashflow_row;
            
            END;
            """;

    public final static SqlAndMetadata UPDATE_CONFIRMATION_STATUS__MERGE_INTO = new SqlAndMetadata(2, UPDATE_CONFIRMATION_STATUS__MERGE_INTO_SQL);
    public final static SqlAndMetadata UPDATE_CONFIRMATION_STATUS__ANONYMOUS_PROCEDURE = new SqlAndMetadata(1, UPDATE_CONFIRMATION_STATUS__ANONYMOUS_PROCEDURE_SQL);
}
