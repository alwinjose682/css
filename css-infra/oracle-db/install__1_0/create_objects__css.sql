--set echo off scan on feedback off verify off
define DATA_TS=&1
define INDEX_TS=&2

prompt *** CREATING SEQUENCES ***
prompt cashflow_seq
@@sequences/cashflow_seq

prompt cf_consumer_audit_seq
@@sequences/cf_consumer_audit_seq

prompt *** CREATING TABLES ***
prompt cashflow
@@tables/cashflow

prompt cashflow_consumer_audit
@@tables/cashflow_consumer_audit

prompt trade_link
@@tables/trade_link

prompt *** PROVIDING GRANTS ***
@@grants/grants_css

--set echo on feedback on verify on
exit
