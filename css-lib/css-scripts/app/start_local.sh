#!/bin/bash
set -euo pipefail

# Common project vars
. ../01_common_env.sh
# Local environment and local spring profiles
. ../02_runtime_env.sh -e local -p local,local-oracle

# Start the app
if [ $# -eq 6 ]; then
  ./start.sh "$1" "$2" "$3" "$4" "$5" "$6"
elif [ $# -eq 1 ]; then
  ./start.sh "-i" "$1"
else
  echo "ERROR: Incorrect number of parameters passed.
        Mandatory Parameters:
          - For Containerized App:
                                1-2)  -i|--identifier: The container image reference (ex: alw.io/css/db-cache-data-loader:1.0.0-SNAPSHOT)
                                3-4)  -c|--containerized [buildpack|non-buildpack],
                                5-6)  -p|--portMapping (ex:8081:8080)
          - For Non-Containerized App:
                                1)    app-name (ex: trade-consumer)
          Actual number of parameters received: $#" >&2
  exit 1
fi
