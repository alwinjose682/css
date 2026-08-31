#!/bin/bash
set -euo pipefail

# Common project vars
. ../01_proj_vars.sh
# Local environment and local spring profiles
. ../02_proj_env_vars.sh -e local -p local,local-oracle

# Start the app
if [ $# -eq 2 ]; then
  ./start.sh "$1" "$2"
elif [ $# -eq 1 ]; then
  ./start.sh "$1"
else
  echo "ERROR: Incorrect number of parameters passed. Num: $#" >&2
  exit 1
fi
