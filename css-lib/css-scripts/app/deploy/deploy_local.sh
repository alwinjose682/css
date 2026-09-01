#!/bin/bash
set -euo pipefail

function findJarIn(){
      jarNotFound="T"
      local searchDir=$1
      buildArtifact=$(find "${searchDir}" -type f -name "${appDirName}-*-jar-with-dependencies.jar")

      if [ -z "${buildArtifact}" ];then
        jarFileCount=$(find "${searchDir}" -type f -name "${appDirName}-*.jar" | wc -l)
        if [ ! ${jarFileCount} -eq 1 ];then
          echo "WARN: Exactly one jar file, ending with '.jar', should be present in the directory: ${searchDir}, count: ${jarFileCount}" >&2
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
  appBinDir="${PROJ_APP_DIR}/${appDir}"               # Ex: css/app
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
          echo "WARN: App JAR not found in maven build directory. Using existing jar file in the app's bin directory from previous build" >&2
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
appDir="$1"
appDirName="$2"

deploy_local
echo "${appBinDir} ${appJar}"
