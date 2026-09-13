#!/bin/bash
set -euo pipefail

function perform_local_containerized_app_start(){
  if [ -z "${1}" ];then
    echo "ERROR: A tag based OCI image reference is not provided. Example of an OCI image reference: 'alw.io/css/db-cache-data-loader:1.0.0-SNAPSHOT'" >&2
    exit 1
  elif [ -z "${portMapping}" ];then
    echo "ERROR: Port mapping for running the containerized app is not provided" >&2
    exit 1
  fi

  imgRef="${1}"

  # Source env for containerized app
  . ../containerized_app_env.sh "${imgRef}"
  # Source JVM Args
  . ../jvm_args.sh "${PROJ_PROFILES}" "${cfgDir}" "${cfgDirRoot}" "${certDir}" "true" "${isBuildpackUsed}"

  # Print info
  echo "\
  CONFIG_STAGE  :      ${CONFIG_STAGE}
  PROJ_PROFILES :      ${PROJ_PROFILES}
  CONTAINERIZED :      ${isContainerized}, is_buildpack_used: ${isBuildpackUsed}
  APP_CFG_DIR   :      ${cfgDir}
  CERT_DIR      :      ${certDir}
  APP_LOG_DIR   :      ${appRootDir}/logs
  JFR_REC_DIR   :      ${appRootDir}/recordings
  VM_ARG_FILES  :      ${argFilesForDisplay}
  "

  # Start the containerized app locally. NOTE: The network mode used is host. No network is created
  . ../detect_container_engine.sh
  /bin/bash -cx '
    set -euo pipefail
    containerEngine="${1}"
    cfgDirRoot="${2}"
    jvmArgs=("${3}")
    portMapping="${4}"
    imgRef="${5}"

    ${containerEngine} \
      run \
      -d \
      --rm \
      --network=host \
      -v css-config:${cfgDirRoot}:ro,z \
      -v scratch-vol:/css/tmp:U \
      -e JDK_JAVA_OPTIONS="${jvmArgs[@]}" \
      "${imgRef}"
  ' "run_app_image__sh" ${containerEngine} "${cfgDirRoot}" "${jvmArgs[*]}" "${portMapping}" "${imgRef}"

# NOTE: Mounting a writable /css/tmp directory with proper ownership and read and write permissions
#       IMP: All the CSS java spring boot apps are configured to use './tmp' as the '-Djava.io.tmpdir'. Check the vmArgs files.
#
# To mount tmpfs
#       --user 1002:1000 \
#       --mount type=tmpfs,destination=/css/tmp,tmpfs-mode=0700,uid=1002,gid=1000 \
#
# To mount an ephemeral disk backed volume with auto chown(:U)
#       --user 1002:1000 \
#       -v scratch-vol:/css/tmp:U \
}

function perform_local_app_start(){
  if [ -z "${1}" ];then
    echo "ERROR: App directory is not provided. Example of an app directory: 'trade-consumer'" >&2
    exit 1
  fi


  # Note: 'appDir' is not the complete dir path. Its only the path relative to the project root directory
  local appDir="${1}"

  # Format 'appDir' input
  ## Strip off all leading "../"
  while [[ "${appDir}" == *'../'* ]];do
    appDir=$(echo "${appDir#../}");
  done;

  ## Strip off one succeeding '/'
  appDir=$(echo "${appDir%/}");

  local appDirName="$(basename "${appDir}")"
  local appCfgDir="${APP_CFG_DIR_ROOT}/${appDir}"

  # Source JVM Args
  . ../jvm_args.sh "${PROJ_PROFILES}" "${appCfgDir}" "${APP_CFG_DIR_ROOT}" "${CERT_DIR}" "${isContainerized}" "${isBuildpackUsed}"

  # Print info
  echo "\
  PROJ_DIR      :      ${PROJ_DIR}
  CONFIG_STAGE  :      ${CONFIG_STAGE}
  PROJ_PROFILES :      ${PROJ_PROFILES}
  CONTAINERIZED :      ${isContainerized}, is_buildpack_used: ${isBuildpackUsed}
  APP_CFG_DIR   :      ${appCfgDir}
  CERT_DIR      :      ${CERT_DIR}
  APP_LOG_DIR   :      ${PROJ_APP_DIR}/${appDir}/logs
  JFR_REC_DIR   :      ${PROJ_APP_DIR}/${appDir}/recordings
  "

  # Execute the deployment strategy and get the 'bin' directory and 'jar' name
  echo "INFO: Performing deployment for environment: ${CONFIG_STAGE}"
  if [ "${CONFIG_STAGE}" == "local" ];then
    deployResult=$(./deploy/deploy_local.sh "${appDir}" "${appDirName}")
    # read -r -a appBinAndJar <<< "${deployResult}"
    read -r appBinDir appJar <<< "${deployResult}"
  else
    echo "ERROR: Unable to determine deployment strategy due to invalid CONFIG_STAGE: {CONFIG_STAGE}" >&2
    exit 1
  fi

  # Create custom 'tmp' directory for the app instead of using standard linux '/tmp'
  mkdir -p ${appBinDir}/tmp

  # Start the non-containerized app locally
  ./start_app.sh -d "${appBinDir}" -j "${appJar}" -i "${argFilesForDisplay}" "${jvmArgs[@]}"
}

# MAIN
# NOTE:
#       The caller must source '01_common_env.sh' and '02_runtime_env.sh' by specifying the appropriate values.
#       'strict mode(set -euo pipefail)' is enabled for this bash script to catch such mistakes
#       'jvm_args.sh' is sourced by this script

isContainerized="false"
isBuildpackUsed="true"

while [[ $# -gt 0 ]]; do
  case "$1" in
    -c|--containerized)
      isContainerized="true"

      if [ "${2}" == "buildpack" ];then
        isBuildpackUsed="true"
      elif [ "${2}" == "non-buildpack" ];then
        isBuildpackUsed="false"
      else
        echo "ERROR: For starting a containerized app, it must be specified whether the OCI image is created using buildpack or not. Allowed values: 'buildpack' and 'non-buildpack'" >&2
        exit 1
      fi

      shift 2
      ;;
    -p|--portMapping)
      portMapping="${2}"
      shift 2
      ;;
    -i|--identifier)
      appIdentifier="${2}" # The app identifier can be either 1) the 'appDir' name or the 2) container image reference. Anything else is invalid
      shift 2
      ;;
    -*)
      echo "ERROR: Unknown option $1" >&2
      exit 1
      ;;
  esac
done

# 1. Basic verifications
if [ -z "${appIdentifier}" ];then
  echo "ERROR: App directory name or tag based OCI image reference is not provided. Example of an app directory/OCI Image: 'trade-consumer' / 'alw.io/css/db-cache-data-loader:1.0.0-SNAPSHOT'" >&2
  exit 1
elif [ -z "${APP_CFG_DIR_ROOT}" ] || [ -z "${CONFIG_STAGE}" ] || [ -z "${PROJ_APP_DIR}" ] || [ -z "${CERT_DIR}" ] || [ -z "${PROJ_PROFILES}" ];then
  echo "ERROR: APP_CFG_DIR_ROOT, CONFIG_STAGE, PROJ_APP_DIR, CERT_DIR and PROJ_PROFILES variables must be set" >&2
  exit 1
fi

if [[ $isContainerized == "true" ]] && [[ $CONFIG_STAGE == "local" ]];then
  perform_local_containerized_app_start "${appIdentifier}"
elif [[ $CONFIG_STAGE == "local" ]];then
  perform_local_app_start "${appIdentifier}"
else
  echo "ERROR: Invalid start configurations. This script can be used to start only local-containerized or local-non-containerized app. containerized: ${isContainerized}, config_stage: ${CONFIG_STAGE}" >&2
  exit 1
fi
