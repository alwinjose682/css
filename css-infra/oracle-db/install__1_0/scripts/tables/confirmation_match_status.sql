CREATE TABLE confirmation_match_status(
id NUMBER(19) NOT NULL,                                                                         -- { @Id | id | nullable = false }
conf_request_id NUMBER(19) NOT NULL,
contra_pair_req_id NUMBER(19),
trade_id NUMBER(19) NOT NULL,                                                                   -- { tradeId | nullable = false }
trade_version NUMBER(10) NOT NULL,                                                              -- { tradeVersion | nullable = false }
trade_leg_id NUMBER(19) NOT NULL,                                                               -- { tradeLegId | nullable = false }
trade_leg_version NUMBER(10) NOT NULL,                                                          -- { tradeLegVersion | nullable = false }
match_event_id NUMBER(19),                                                                      -- { matchEventId | nullable = false }
match_event_version NUMBER(10),                                                                 -- { matchEventVersion | nullable = false }
nostro_id VARCHAR2(5) NOT NULL,                                                                 -- { nostroId }
ssi_id VARCHAR2(8) NOT NULL,                                                                    -- { ssiId }
sent_or_recd VARCHAR2(4) NOT NULL CONSTRAINT conf_strd_chk CHECK(status IN ('SENT','RECD'))     -- { sentOrRecd }
match_status VARCHAR2(18),                                                                      -- { matchStatus }
match_date DATE,                                                                                -- { matchDate }
input_date_time TIMESTAMP(3) NOT NULL,                                                          -- { dateTime }
);

alter table confirmation_match_status add constraint conf_m_stat_pk PRIMARY KEY(id);
create INDEX conf_m_stat_idx1 on confirmation_match_status(trade_id, trade_version, trade_leg_id, trade_leg_version);
create INDEX conf_m_stat_idx2 on confirmation_match_status(request_id);
-- NOTE_1: trade_id, trade_version, trade_leg_id, trade_leg_version will NOT be UNIQUE for this table
-- NOTE_2: The confirmations could be matched on a different nostroId  or ssiId than the one selected by CSS trade-consumer