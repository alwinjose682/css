#!/bin/bash
set -euo pipefail

imgRef="${1}"
projectProfiles="${2}"
isBuildpackUsed="${3}"
# Source env for containerized app
. ../containerized_app_env.sh "${imgRef}"
# Source JVM Args
. ../jvm_args.sh "${projectProfiles}" "${cfgDir}" "${cfgDirRoot}" "${certDir}" "true" "${isBuildpackUsed}"
echo "${jvmArgs[@]}"
