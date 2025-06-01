CREATE TABLE cashflow(
cashflow_id NUMBER(19) NOT NULL,           							  				-- { cashflowId | nullable = false }
cashflow_version NUMBER(10) NOT NULL,              							  		-- { cashflowVersion | nullable = false }
latest VARCHAR2(1) NOT NULL CONSTRAINT cf_latest_chk CHECK(latest IN ('Y','N')),      	-- { latest | STRING }
revision_type VARCHAR2(3) NOT NULL CONSTRAINT cf_revision_type_chk CHECK(revision_type IN ('NEW','CAN','COR')),   -- { revisionType | STRING }
fo_cashflow_id NUMBER(19) NOT NULL,                                              	-- { foCashflowID | nullable = false }
fo_cashflow_version NUMBER(10) NOT NULL,                                         	-- { foCashflowVersion | nullable = false }
trade_id NUMBER(19),                                                    		    -- { tradeID | nullable = false }
trade_version NUMBER(10),                                                           -- { tradeVersion | nullable = false}
trade_type VARCHAR2(15),                                                 			-- { tradeType | STRING }
book_code VARCHAR2(5),                                                  			-- { bookCode }
counter_book_code VARCHAR2(5),                                          			-- { counterBookCode }
secondary_ledger_account VARCHAR2(5),                                   			-- { secondaryLedgerAccount }
transaction_type VARCHAR2(10),                                     					-- { transactionType | STRING }
rate NUMBER(30,10),                                                        			-- { rate | scale = PaymentConstants.RATE_SCALE }
value_date DATE,                                                 					-- { valueDate }
--list<tradelinkentity> tradeLinks,                                     			-- { @OneToMany | chk src file for full info }
entity_code VARCHAR2(5),                                                			-- { entityCode }
counterparty_code VARCHAR2(10),                                         			-- { counterpartyCode }
amount NUMBER(* ,5),                                                      			-- { amount | scale = PaymentConstants.AMOUNT_SCALE }
curr_code VARCHAR2(3),                                                 				-- { currCode }
internal VARCHAR2(1) NOT NULL CONSTRAINT cf_internal_chk CHECK(internal IN ('Y','N')), -- { internal | STRING }
nostro_id VARCHAR2(5),                                                   			-- { nostroID }
ssi_id VARCHAR2(8),                                                      			-- { ssiID }
payment_suppression_category VARCHAR2(30) NOT NULL,				        			-- { paymentSuppressionCategory }
input_by VARCHAR2(10),                                                     			-- { inputBy | ORDINAL }
input_by_user_id VARCHAR2(10),                                          			-- { inputByUserID }
input_date_time TIMESTAMP(3)                                         			    -- { inputDateTime }
)
tablespace &DATA_TS
;

ALTER TABLE cashflow ADD CONSTRAINT cf_pk PRIMARY KEY(cashflow_id, cashflow_version) USING INDEX tablespace &INDEX_TS;
ALTER TABLE cashflow ADD CONSTRAINT cf_uniq_1 UNIQUE(cashflow_id, cashflow_version,fo_cashflow_id, fo_cashflow_version);
CREATE INDEX cf_confirmation_idx on cashflow(fo_cashflow_id, fo_cashflow_version, trade_id, trade_version) tablespace &INDEX_TS;
--CREATE INDEX cf_netting_idx on cashflow(value_date, curr_code, payment_suppressed) tablespace &INDEX_TS; -- index for netting is not required as there is no netting window in current design. Netting for CFs of any VD is done on confirmation event
