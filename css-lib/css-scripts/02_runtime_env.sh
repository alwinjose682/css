#! /bin/bash
set -euo pipefail

if [ "$#" -ne 0 ]; then
# -e <param>: environment(ex: prod, uat etc)
# -p <param>: spring profiles. Can be comma separated but without white space(ex: 'prod,prod-oracle', 'uat,uat-oracle')
# The origin of these args could be k8 manifest, docker env value, shell command line etc

  while [[ $# -gt 0 ]]; do
    case "$1" in
      -e|--env)
        CONFIG_STAGE="${2}"
        shift 2
        ;;
      -p|--profile)
        PROJ_PROFILES="${2}"
        shift 2
        ;;
      *)
        echo "ERROR: Invalid option or param ${1}" >&2
        exit 1
        ;;
    esac
  done

  export CONFIG_STAGE
  export PROJ_PROFILES
  # Export the certificate directory path corresponding to the environment
  CERT_DIR="${PROJ_CFG_DIR}/cert/${CONFIG_STAGE}"
  export CERT_DIR
else
  echo "ERROR: CONFIG_STAGE and PROJ_PROFILES are not provided" >&2
  exit 1
fi
