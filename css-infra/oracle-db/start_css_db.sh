#!/bin/bash

function log{
  dt=$(date '+%d/%m/%Y %H:%M:%S');
  echo "[${dt}] $*"
}

# Main
CSS_DB_HOME=/etc/opt/css_db
INIT_INSTALL_DIR=${CSS_DB_HOME}/install__1_0
INIT_INSTALL_FLAG=${INIT_INSTALL_DIR}/init_install_flag

if [ ! -f ${INIT_INSTALL_FLAG} ]; then
  log "Starting initial install of CSS-DB"
  touch ${INIT_INSTALL_FLAG}

    /bin/bash -c " \
    dt=$(date '+%d/%m/%Y %H:%M:%S'); \
    ${INIT_INSTALL_DIR}/install.sh > ${INIT_INSTALL_DIR}/install_out_${dt}.log ; \
    "

   log "Finished initial install of CSS-DB"
fi
