#!/bin/bash
set -euo pipefail

function findJarIn(){
      jarNotFound="T"
      searchDir=$1
      buildArtifact=$(find "${searchDir}" -type f -name "${appDirName}-*-jar-with-dependencies.jar")

      if [ -z "${buildArtifact}" ];then
        jarFileCount=$(find "${searchDir}" -type f -name "${appDirName}-*.jar" | wc -l)
        if [ ! ${jarFileCount} -eq 1 ];then
          echo "Exactly one jar file, ending with '.jar', should be present in the directory: ${searchDir}, count: ${jarFileCount}"
        else
          jarNotFound="F"
          buildArtifact=$(find "${searchDir}" -type f -name "${appDirName}-*.jar")
        fi
      else
        jarNotFound="F"
      fi
}

### Just a local deploy, ie; moving the jar file from maven build directory to app's bin directory
function deploy_local(){
  appBinDir="${PROJ_APP_DIR}/${appDir}"                 # Ex: css/app
  local mvnAppBuildDir="${PROJ_DIR}/${appDir}/target"   # Ex: css/trade-consumer/target

  if [ -d "${mvnAppBuildDir}" ];then
    findJarIn "${mvnAppBuildDir}"
#    buildArtifact=echo $?

    ### Does the following:
    #### - If exists, move the jar file from maven build directory to the app's bin directory
    #### - If no jar file exists in maven build directory, use existing jar file in the app's bin directory from previous build
    #### - assign the 'appJar' variable
    if [ -d "${appBinDir}" ];then
        if [ "${jarNotFound}" == "T" ];then
          findJarIn "${appBinDir}"
          if [ "${jarNotFound}" == "T" ];then
            exit 1;
          fi
          echo "appJar not found in maven build directory. Using existing jar file in the app's bin directory from previous build"
        else
          rm -f "${appBinDir}/*.jar"
          mv "${buildArtifact}" "${appBinDir}"
        fi
    else
        if [ "${jarNotFound}" == "T" ];then
          exit 1;
        fi
        mkdir -p "${appBinDir}"
        mv "${buildArtifact}" "${appBinDir}"
    fi

    appJar=$(basename "${buildArtifact}")

  else
    echo "ERROR: The app build directory does not exist: ${mvnAppBuildDir}" >&2
    exit 1
  fi
}

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
      echo "ERROR: Unknown option $1"
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
  echo "APP_CFG_DIR_ROOT, CONFIG_STAGE, PROJ_APP_DIR, CERT_DIR and PROJ_PROFILES variables must be set"
  exit 1
fi

# 2. Format 'appDir' input
# Note: 'appDir' is not the complete dir path. Its only the path relative to the project root directory
if [ -z "${appDir}" ];then
  echo "App directory is not provided"
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

# 5. Perform local deployment
deploy_local

./start_app.sh -d "${appBinDir}" -j "${appJar}" -i "${argFilesForDisplay}" "${jvmOpts[@]}"
