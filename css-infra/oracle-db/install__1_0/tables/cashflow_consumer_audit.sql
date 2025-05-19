CREATE TABLE cashflow_consumer_audit(
id NUMBER(19) NOT NULL,                             -- { @Id | id }
fo_cashflow_id NUMBER(19),                          -- { foCashflowID }
fo_cashflow_version NUMBER(10),                     -- { foCashflowVersion }
trade_id NUMBER(19),                                -- { tradedID }
trade_version NUMBER(10),                           -- { tradeVersion }
trade_type VARCHAR2(15),                            -- { tradeType | STRING }
status VARCHAR2(15),                                -- { status | STRING }
received_date_time TIMESTAMP(3)                     -- { receivedDateTime }
)
tablespace &DATA_TS
;

ALTER TABLE cashflow_consumer_audit ADD CONSTRAINT cfcon_aud_pk PRIMARY KEY(id) USING INDEX tablespace &INDEX_TS;
