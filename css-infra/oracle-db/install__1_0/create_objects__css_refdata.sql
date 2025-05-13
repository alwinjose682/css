--set echo off scan on feedback off verify off
define DATA_TS=&1
define INDEX_TS=&2

prompt *** CREATING TABLES ***
prompt counterparty
@@tables/counterparty

prompt counterparty_netting_profile
@@tables/counterparty_netting_profile

prompt counterparty_sla_mapping
@@tables/counterparty_sla_mapping

prompt ssi
@@tables/ssi

prompt country
@@tables/country

prompt currency
@@tables/currency

prompt entity
@@tables/entity

prompt book
@@tables/book

prompt nostro
@@tables/nostro

prompt data_load_status
@@tables/data_load_status

prompt *** PROVIDING GRANTS ***
@@grants/grants_css_refdata

--set echo on feedback on verify on
exit
