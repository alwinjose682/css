CREATE TABLE nostro(
nostro_id VARCHAR2(5) NOT NULL,                                                     -- { nostroID | nullable = false }
nostro_version NUMBER(10) NOT NULL,                                                 -- { nostroVersion | nullable = false, length = 10 }
entity_code VARCHAR2(5),                                                            -- { entityCode | nullable = false }
entity_version NUMBER(10),                                                          -- { entityVersion | nullable = false, length = 10 }
curr_code VARCHAR2(3),                                                              -- { currCode | nullable = false }
secondary_ledger_account VARCHAR2(5),                                               -- { secondaryLedgerAccount | nullable = false }
is_primary VARCHAR2(1) NOT NULL CONSTRAINT nstr_pmy_chk CHECK(is_primary IN('Y','N')),         -- { primary | nullable = false, length = 1 | STRING }
bene_bic VARCHAR2(11),                                                              -- { beneBic }
bank_bic VARCHAR2(11),                                                              -- { bankBic }
bank_account VARCHAR2(20),                                                          -- { bankAccount }
bank_line1 VARCHAR2(30),                                                            -- { bankLine1 }
corr_bic VARCHAR2(11),                                                              -- { corrBic }
corr_account VARCHAR2(20),                                                          -- { corrAccount }
corr_line1 VARCHAR2(30),                                                            -- { corrLine1 }
cut_off_time TIMESTAMP(0),                                                          -- { cutOffTime }
cut_in_hours_offset NUMBER(2),                                                      -- { cutInHoursOffset | length = 10 }
payment_limit NUMBER(*,0),                                                          -- { paymentLimit | scale = 5 }
active VARCHAR2(1) NOT NULL CONSTRAINT nstr_active_chk CHECK(active IN('Y','N')),        -- { active | nullable = false, length = 1 | STRING }
entry_time TIMESTAMP(3)                                                             -- { entryTime }
);

ALTER TABLE nostro ADD CONSTRAINT nstr_pk PRIMARY KEY(nostro_id, nostro_version);
ALTER TABLE nostro ADD CONSTRAINT nstr_ent_fk FOREIGN KEY(entity_code, entity_version) REFERENCES entity(entity_code, entity_version);
--CREATE INDEX nstr_et_cr_idx on nostro(entity_code, entity_version, curr_code);
