#!/bin/bash

# Build H2 DB to be run in server mode. OracleDB can be used instead of H2 when needed
./install.sh ../../../css-infra/h2-server/

# Build all java components
./install.sh ../../../css-lib/css-shared/
./install.sh ../../../css-lib/data-generator-shared/
./install.sh ../../../refdata-generator/
./install.sh ../../../db-cache-data-loader/
./install.sh ../../../fo-simulator/
