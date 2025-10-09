--DB Info
SELECT * FROM V$DATABASE; -- NAME, CDB

--Tablespace
SELECT DISTINCT sgm.TABLESPACE_NAME , dtf.FILE_NAME
FROM DBA_SEGMENTS sgm
JOIN DBA_DATA_FILES dtf ON (sgm.TABLESPACE_NAME = dtf.TABLESPACE_NAME)

SELECT * FROM DBA_DATA_FILES
SELECT * FROM DBA_TABLESPACES

--Users
select * from dba_users;

--Tables
SELECT * FROM all_tables WHERE tablespace_name='CSS_DATA'

--Truncate all ref data tables, owner="CSS_REFDATA"
truncate table DATA_LOAD_STATUS;
truncate table BOOK;
truncate table NOSTRO;
truncate table ENTITY;
truncate table COUNTERPARTY_NETTING_PROFILE;
truncate table COUNTERPARTY_SLA_MAPPING;
truncate table SSI;
truncate table COUNTERPARTY;
truncate table COUNTRY;
truncate table CURRENCY;
