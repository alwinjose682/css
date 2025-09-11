CREATE TABLE counterparty(
counterparty_code VARCHAR2(10) NOT NULL,        					  				-- { counterpartyCode | nullable = false }
counterparty_version NUMBER(10) NOT NULL,      						  				-- { counterpartyVersion | nullable = false, length = 10 }
counterparty_name VARCHAR2(20),                                         			-- { counterpartyName }
parent VARCHAR2(1) NOT NULL CONSTRAINT cpty_parent_chk CHECK(parent IN('Y','N')),        -- { parent | nullable = false, length = 1 | STRING }
internal VARCHAR2(1) NOT NULL CONSTRAINT cpty_internal_chk CHECK(internal IN('Y','N')),  -- { internal | nullable = false, length = 1 | STRING }
parent_counterparty_code VARCHAR2(10),                                  			-- { parentCounterpartyCode }
counterparty_type VARCHAR2(25),                                         			-- { counterpartyType }
bic_code VARCHAR2(11),                                                  			-- { bicCode }
entity_code VARCHAR2(5),                                               				-- { entityCode }
address_line1 VARCHAR2(35),                                             			-- { addressLine1 }
address_line2 VARCHAR2(35),                                             			-- { addressLine2 }
city VARCHAR2(35),                                                      			-- { city }
state VARCHAR2(35),                                                     			-- { state }
country VARCHAR2(20),                                                   			-- { country }
region VARCHAR2(5),                                                    				-- { region }
active VARCHAR2(1) NOT NULL CONSTRAINT cpty_active_chk CHECK(active IN('Y','N')),        -- { active | nullable = false, length = 1 | STRING }
entry_time TIMESTAMP(3)                                                 			-- { entryTime }
);

ALTER TABLE counterparty ADD CONSTRAINT cpty_pk PRIMARY KEY(counterparty_code, counterparty_version);
--CREATE INDEX cpty_pcp_idx on counterparty(parent_counterparty_code);
