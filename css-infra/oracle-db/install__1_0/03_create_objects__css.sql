--set echo off scan on feedback off verify off

prompt *** CREATING SEQUENCES ***
prompt cashflow_seq
@scripts/sequences/cashflow_seq

prompt css_common_seq
@scripts/sequences/css_common_seq

prompt *** CREATING TABLES ***
prompt cashflow
@scripts/tables/cashflow

prompt cashflow_rejection
@scripts/tables/cashflow_rejection

prompt trade_link
@scripts/tables/trade_link

prompt *** PROVIDING GRANTS ***
@scripts/grants/grants_css

--set echo on feedback on verify on
exit
