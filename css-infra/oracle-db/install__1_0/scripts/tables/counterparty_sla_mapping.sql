CREATE TABLE counterparty_sla_mapping(
mapping_id NUMBER(19) NOT NULL,            										-- { mappingID | length = 19, nullable = false }
mapping_version NUMBER(10) NOT NULL,       										-- { mappingVersion | length = 10, nullable = false }
counterparty_code VARCHAR2(10),                                              	-- { counterpartyCode | nullable = false }
counterparty_version NUMBER(10),                                             	-- { counterpartyVersion | nullable = false }
entity_code VARCHAR2(5),                                                    	-- { entityCode }
curr_code VARCHAR2(3),                                                      	-- { currCode }
secondary_ledger_account VARCHAR2(5),                                       	-- { secondaryLedgerAccount }
active VARCHAR2(1) NOT NULL CONSTRAINT cpsm_active_chk CHECK(active IN('Y','N')),    -- { active | length = 1, nullable = false | STRING }
entry_time TIMESTAMP(3)                                                    		-- { entryTime }
);

ALTER TABLE counterparty_sla_mapping ADD CONSTRAINT cpsm_pk PRIMARY key(mapping_id, mapping_version);
ALTER TABLE counterparty_sla_mapping ADD CONSTRAINT cpsm_cp_fk FOREIGN KEY(counterparty_code, counterparty_version) REFERENCES counterparty(counterparty_code, counterparty_version);
--CREATE INDEX cpsm_cp_et_idx on counterparty_sla_mapping(counterparty_code, counterparty_version, entity_code);
