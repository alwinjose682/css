#!/bin/bash
set -euo pipefail

# Build all java components
./build.sh clean install dir:css-lib/css-shared/
./build.sh clean install dir:css-lib/data-generator-shared/
./build.sh clean install dir:refdata-generator
./build.sh clean install dir:css-infra/ignite-cache/
./build.sh clean install dir:db-cache-data-loader
./build.sh clean install dir:trade-publisher
./build.sh clean install dir:trade-consumer

# Build H2 DB to be run in server mode. OracleDB can be used instead of H2 when needed with config changes in relevant places like pom etc
./build.sh clean install dir:css-infra/h2-server/
