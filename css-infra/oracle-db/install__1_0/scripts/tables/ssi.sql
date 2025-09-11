CREATE TABLE ssi(
ssi_id VARCHAR2(5) NOT NULL,                                                    -- { ssiID | nullable = false }
ssi_version NUMBER(10) NOT NULL,                                                -- { ssiVersion | length = 10, nullable = false }
counterparty_code VARCHAR2(10),                                                 -- { counterpartyCode | nullable = false }
counterparty_version NUMBER(10),                                                -- { counterpartyVersion | nullable = false, length = 10 }
curr_code VARCHAR2(3),                                                          -- { currCode | nullable = false, length = 3 }
product VARCHAR2(15),                                                           -- { product | nullable = false }
is_primary VARCHAR2(1) NOT NULL CONSTRAINT ssi_pmy_chk CHECK(is_primary IN('Y','N')),     -- { primary | nullable = false, length = 1 | STRING }
bene_type VARCHAR2(30),                                                         -- { beneType }
bank_bic VARCHAR2(11),                                                          -- { bankBic }
bank_account VARCHAR2(20),                                                      -- { bankAccount }
bank_line1 VARCHAR2(30),                                                        -- { bankLine1 }
corr_bic VARCHAR2(11),                                                          -- { corrBic }
corr_account VARCHAR2(20),                                                      -- { corrAccount }
corr_line1 VARCHAR2(30),                                                        -- { corrLine1 }
active VARCHAR2(1) NOT NULL CONSTRAINT ssi_active_chk CHECK(active IN('Y','N')),    -- { active | nullable = false, length = 1 | STRING }
entry_time TIMESTAMP(3)                                                         -- { entryTime }
)
tablespace &DATA_TS
;

ALTER TABLE ssi ADD CONSTRAINT ssi_pk PRIMARY KEY(ssi_id, ssi_version) USING INDEX tablespace &INDEX_TS;
ALTER TABLE ssi ADD CONSTRAINT ssi_cp_fk FOREIGN KEY(counterparty_code, counterparty_version) REFERENCES counterparty(counterparty_code, counterparty_version);
--CREATE INDEX ssi_cp_cr on ssi(counterparty_code, counterparty_version, curr_code) tablespace &INDEX_TS;
