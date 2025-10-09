CREATE TABLE counterparty_netting_profile(
netting_profile_id NUMBER(19) NOT NULL,        																				-- { nettingProfileID | nullable = false, length = 19 }
netting_profile_version NUMBER(10) NOT NULL,   																				-- { nettingProfileVersion | nullable = false, length = 10 }
counterparty_code VARCHAR2(10),                                                        										-- { counterpartyCode | nullable = false }
counterparty_version NUMBER(10),                                                       										-- { counterpartyVersion | nullable = false, length = 10 }
product VARCHAR2(15),                                                                  										-- { product | nullable = false }
netting_type VARCHAR2(20),                                                             										-- { nettingType }
net_by_parent_counterparty_code VARCHAR2(1) NOT NULL CONSTRAINT cpnp_nbp_chk CHECK(net_by_parent_counterparty_code IN('Y','N')), -- { netByParentCounterpartyCode | nullable = false, length = 1 | STRING }
net_for_any_entity VARCHAR2(1) NOT NULL CONSTRAINT cpnp_nae_chk CHECK(net_for_any_entity IN('Y','N')),                           -- { netForAnyEntity | nullable = false, length = 1 | STRING }
entity_code VARCHAR2(5),                                                              										-- { entityCode }
active VARCHAR2(1) NOT NULL CONSTRAINT cpnp_active_chk CHECK(active IN('Y','N')),                                                -- { active | nullable = false, length = 1 | STRING }
entry_time TIMESTAMP(3)                                                                										-- { entryTime }
);

ALTER TABLE counterparty_netting_profile ADD CONSTRAINT cpnp_pk PRIMARY KEY(netting_profile_id, netting_profile_version);
ALTER TABLE counterparty_netting_profile ADD CONSTRAINT cpnp_cp_fk FOREIGN KEY(counterparty_code, counterparty_version) REFERENCES counterparty(counterparty_code, counterparty_version);
--CREATE INDEX cpnp_cp_pt_idx on counterparty_netting_profile(counterparty_code, counterparty_version, product);
