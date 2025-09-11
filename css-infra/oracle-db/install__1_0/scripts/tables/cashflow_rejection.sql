CREATE TABLE cashflow_rejection(
id NUMBER(19) NOT NULL,
fo_cashflow_id NUMBER(19) NOT NULL,
fo_cashflow_version NUMBER(10) NOT NULL,
trade_id NUMBER(19),
trade_version NUMBER(10),
trade_type VARCHAR2(15),
value_date DATE,
entity_code VARCHAR2(5),
counterparty_code VARCHAR2(10),
amount NUMBER(* ,5),
curr_code VARCHAR2(3),
exception_type VARCHAR2(15),
exception_category VARCHAR2(15),
exception_sub_category VARCHAR2(50),
msg VARCHAR2(800),
replayable VARCHAR2(1) NOT NULL CONSTRAINT cfrej_replayable_chk CHECK(replayable IN ('Y','N')),
num_of_retries NUMBER(10),
created_date_time TIMESTAMP(3),
input_by VARCHAR2(10),
updated_date_time TIMESTAMP(3)
);

ALTER TABLE cashflow_rejection ADD CONSTRAINT cfrej_pk PRIMARY KEY(id);
CREATE INDEX cfrej_replay_idx on cashflow_rejection(value_date, curr_code, replayable);
