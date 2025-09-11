--set echo off scan on feedback off verify off

prompt *** CREATING TABLES ***
prompt counterparty
@scripts/tables/counterparty

prompt counterparty_netting_profile
@scripts/tables/counterparty_netting_profile

prompt counterparty_sla_mapping
@scripts/tables/counterparty_sla_mapping

prompt ssi
@scripts/tables/ssi

prompt country
@scripts/tables/country

prompt currency
@scripts/tables/currency

prompt entity
@scripts/tables/entity

prompt book
@scripts/tables/book

prompt nostro
@scripts/tables/nostro

prompt data_load_status
@scripts/tables/data_load_status

prompt *** PROVIDING GRANTS ***
@scripts/grants/grants_css_refdata

--set echo on feedback on verify on
exit
