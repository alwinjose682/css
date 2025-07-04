#!/bin/bash

echo "***************************"
echo "* STARTING CSS DB INSTALL *"
echo "***************************"

DB_SERVICE_NAME='XEPDB1'
DATA_TS='CSS_DATA'
INDEX_TS='CSS_INDEX'

SYS_USERNAME='sys'
#SYS_PASSWORD='freepass'
SYS_PASSWORD='sysdbapass'
CSS_USERNAME='css'
CSS_PASSWORD='freepass'
CSS_REFDATA_USERNAME='css_refdata'
CSS_REFDATA_PASSWORD='freepass'


echo "***********************"
echo "* CREATING TABLESPACE *"
echo "***********************"
sqlplus -s -l ${SYS_USERNAME}/${SYS_PASSWORD}@${DB_SERVICE_NAME} as sysdba @create_tablespaces ${DATA_TS} ${INDEX_TS}

echo "*****************"
echo " CREATING USERS *"
echo "*****************"
sqlplus -s -l ${SYS_USERNAME}/${SYS_PASSWORD}@${DB_SERVICE_NAME} as sysdba @create_users ${DATA_TS} ${INDEX_TS}

echo "***************************"
echo "* CREATING CSS DB OBJECTS *"
echo "***************************"
sqlplus -s -l ${CSS_USERNAME}/${CSS_PASSWORD}@${DB_SERVICE_NAME} @create_objects__css ${DATA_TS} ${INDEX_TS}

echo "***********************************"
echo "* CREATING CSS_REFDATA DB OBJECTS *"
echo "***********************************"
sqlplus -s -l ${CSS_REFDATA_USERNAME}/${CSS_REFDATA_PASSWORD}@${DB_SERVICE_NAME} @create_objects__css_refdata ${DATA_TS} ${INDEX_TS}

echo "**************************"
echo " FINISHED CSS DB INSTALL *"
echo "**************************"


## https://docs.oracle.com/en/database/oracle/oracle-database/21/sqlrf/GRANT.html
