#!/bin/bash
set -euo pipefail

# MAIN
# NOTE:
#       The caller must source '01_proj_vars.sh' and '02_proj_env_vars.sh' by specifying the appropriate values.
#       'strict mode(set -euo pipefail)' is enabled for this bash script to catch such mistakes
#       '03_jvm_opts.sh' is sourced by this script

containerized="false"
while [[ $# -gt 0 ]]; do
  case "$1" in
    -c|--containerized)
      containerized="true"
      shift
      ;;
    -*)
      echo "ERROR: Unknown option $1" >&2
      exit 1
      ;;
    *)
      appDir="$*"
      break
      ;;
  esac
done

# 1. Basic verifications
if [ -z "${APP_CFG_DIR_ROOT}" ] || [ -z "${CONFIG_STAGE}" ] || [ -z "${PROJ_APP_DIR}" ] || [ -z "${CERT_DIR}" ] || [ -z "${PROJ_PROFILES}" ];then
  echo "ERROR: APP_CFG_DIR_ROOT, CONFIG_STAGE, PROJ_APP_DIR, CERT_DIR and PROJ_PROFILES variables must be set" >&2
  exit 1
fi

# 2. Format 'appDir' input
# Note: 'appDir' is not the complete dir path. Its only the path relative to the project root directory
if [ -z "${appDir}" ];then
  echo "ERROR: App directory is not provided" >&2
  exit 1
fi

## Strip off all leading "../"
while [[ "${appDir}" == *'../'* ]];do
  appDir=$(echo "${appDir#../}");
done;

## Strip off one succeeding '/'
appDir=$(echo "${appDir%/}");

appDirName="$(basename "${appDir}")"
appCfgDir="${APP_CFG_DIR_ROOT}/${appDir}"

# 3. Source JVM Options(jvmOpts)
. ../03_jvm_opts.sh "${appCfgDir}"

# 4. Print info
echo "\
PROJ_DIR      :      ${PROJ_DIR}
CONFIG_STAGE  :      ${CONFIG_STAGE}
PROJ_PROFILES :      ${PROJ_PROFILES}
APP_CFG_DIR   :      ${appCfgDir}
CERT_DIR      :      ${CERT_DIR}
APP_LOG_DIR   :      ${PROJ_APP_DIR}/${appDir}/logs
JFR_REC_DIR   :      ${PROJ_APP_DIR}/${appDir}/recordings
"

# 5. Execute the deployment strategy and get the 'bin' directory and 'jar' name
echo "INFO: Performing deployment for environment: ${CONFIG_STAGE}"
if [ "${CONFIG_STAGE}" == "local" ];then
  deployResult=$(./deploy/deploy_local.sh "${appDir}" "${appDirName}")
  # read -r -a appBinAndJar <<< "${deployResult}"
  read -r appBinDir appJar <<< "${deployResult}"
else
  echo "ERROR: Unable to determine deployment strategy due to invalid CONFIG_STAGE: {CONFIG_STAGE}" >&2
  exit 1
fi

# 6. Create custom 'tmp' directory for the app instead of using standard linux '/tmp'
mkdir -p ${appBinDir}/tmp

# 7. Start the app
./start_app.sh -d "${appBinDir}" -j "${appJar}" -i "${argFilesForDisplay}" "${jvmOpts[@]}"
