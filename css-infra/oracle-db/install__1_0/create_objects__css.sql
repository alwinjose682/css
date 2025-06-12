--set echo off scan on feedback off verify off
define DATA_TS=&1
define INDEX_TS=&2

prompt *** CREATING SEQUENCES ***
prompt cashflow_seq
@@sequences/cashflow_seq

prompt css_common_seq
@@sequences/css_common_seq

prompt *** CREATING TABLES ***
prompt cashflow
@@tables/cashflow

prompt cashflow_rejection
@@tables/cashflow_rejection

prompt trade_link
@@tables/trade_link

prompt *** PROVIDING GRANTS ***
@@grants/grants_css

--set echo on feedback on verify on
exit
