#!/bin/bash

# Build all java components
./install.sh css-lib/css-shared/
./install.sh css-lib/data-generator-shared/
./install.sh refdata-generator
./install.sh css-infra/ignite-cache/
./install.sh db-cache-data-loader
./install.sh fo-simulator
./install.sh cashflow-consumer

# Build H2 DB to be run in server mode. OracleDB can be used instead of H2 when needed with config changes in relevant places like pom etc
./install.sh css-infra/h2-server/
