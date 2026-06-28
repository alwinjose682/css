CREATE TABLE trade_link(
id NUMBER(19) NOT NULL,
trade_id NUMBER(19) NOT NULL,              -- { cashflowId }
trade_version NUMBER(10) NOT NULL,         -- { cashflowVersion }
link_type VARCHAR2(15) NOT NULL,        -- { linkType }
related_reference VARCHAR2(15),         -- { relatedReference }
related_trade_id NUMBER(19),
related_trade_version NUMBER(10)
);
ALTER TABLE trade_link ADD CONSTRAINT tde_lk_pk PRIMARY KEY(id);
--ALTER TABLE trade_link ADD CONSTRAINT tde_lk_cf_fk FOREIGN KEY(cf_id, cf_version) REFERENCES cashflow(cashflow_id, cashflow_version);