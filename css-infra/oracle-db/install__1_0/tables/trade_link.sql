CREATE TABLE trade_link(
id NUMBER(19) NOT NULL,
cf_id NUMBER(19) NOT NULL,              -- { cashflowId }
cf_version NUMBER(10) NOT NULL,         -- { cashflowVersion }
link_type VARCHAR2(15),                 -- { linkType }
related_reference VARCHAR2(15)          -- { relatedReference }
)
tablespace &DATA_TS
;

ALTER TABLE trade_link ADD CONSTRAINT tde_lk_pk PRIMARY KEY(id) USING INDEX TABLESPACE &INDEX_TS;
ALTER TABLE trade_link ADD CONSTRAINT tde_lk_cf_fk FOREIGN KEY(cf_id, cf_version) REFERENCES cashflow(cashflow_id, cashflow_version);