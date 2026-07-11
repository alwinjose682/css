CREATE TABLE confirmation_match_status(
id NUMBER(19) NOT NULL,                                                                         -- { @Id | id | nullable = false }
cashflow_id NUMBER(19) NOT NULL,           							  				            -- { cashflowId | nullable = false }
cashflow_version NUMBER(10) NOT NULL,              							  		            -- { cashflowVersion | nullable = false }
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
alter table confirmation_match_status add constraint conf_m_stat_fk FOREIGN KEY(cashflow_id, cashflow_version) references cashflow(cashflow_id, cashflow_version);
create INDEX conf_m_stat_idx_1 on confirmation_match_status(cashflow_id, cashflow_version);
-- cashflow_id, cashflow_version will NOT be UNIQUE for this table
-- The confirmations could be matched on a different nostroId  or ssiId than the one selected by CSS trade-consumer