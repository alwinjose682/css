#!/bin/bash

function startApp(){
  echo ""

  ### App specific VM args (JVM Args specified in a file)
  #### https://docs.oracle.com/en/java/javase/24/docs/specs/man/java.html#java-command-line-argument-files
  vmArgsFile="${appCfgDir}/vmArgs"

  ### Java cmd
  javaCmd="\
java \
@${vmArgsFile} \
-Dspring.config.location=${APP_CFG_DIR_ROOT}/,${appCfgDir}/ \
-Dlogging.config=${appCfgDir}/logback-spring.xml \
-Dspring.profiles.active=${PROJ_PROFILES} \
-Dcss.cert.path=${CERT_DIR} \
-jar ${appBinDir}/${appJar} \
"

echo "\
JAR_FILE      :      ${appBinDir}/${appJar}
VM_ARGS_FILE  :      ${vmArgsFile}
JAVA_CMD      :      ${javaCmd}\
"

  ### Start the app
  ### It is important to move to the app's bin directory because java command line argument file only recognizes relative paths with respect to current directory
  echo ""
  java -version
  /bin/bash -c " \
cd ${appBinDir} ; \
${javaCmd} \
"

}

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
function deploy(){
  mvnAppBuildDir="${PROJ_DIR}/${appDir}/target"
  if [ -d "${mvnAppBuildDir}" ];then
    findJarIn ${mvnAppBuildDir}
#    buildArtifact=echo $?

    ### Does the following
    #### - If exists, move the jar file from maven build directory to the app's bin directory
    #### - If no jar file exists in maven build directory, use existing jar file in the app's bin directory from previous build
    #### - assign the 'appJar' variable
    if [ -d "${appBinDir}" ];then
        if [ "${jarNotFound}" == "T" ];then
          findJarIn ${appBinDir}
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
    echo "The app build directory does not exist: ${mvnAppBuildDir}"
    exit 1
  fi
}

# MAIN
### Get the project specific vars
. ../proj_specific_vars.sh

appDir="${1}" # Note: 'appDir' is not the complete dir path. Its only the path relative to the project root directory
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

appDirName="$(basename ${appDir})"
appCfgDir="${APP_CFG_DIR_ROOT}/${appDir}"

echo "\
PROJ_DIR      :      ${PROJ_DIR}
CONFIG_STAGE  :      ${CONFIG_STAGE}
PROJ_PROFILES :      ${PROJ_PROFILES}
APP_CFG_DIR   :      ${appCfgDir}
CERT_DIR      :      ${CERT_DIR}
APP_LOG_DIR   :      ${PROJ_APP_DIR}/${appDir}/logs
"

if [ -z "${APP_CFG_DIR_ROOT}" ] || [ -z "${CONFIG_STAGE}" ] || [ -z "${PROJ_APP_DIR}" ] || [ -z "${CERT_DIR}" ];then
  echo "APP_CFG_DIR_ROOT, CONFIG_STAGE, PROJ_APP_DIR and CERT_DIR variables must be set"
  exit 1
fi

appBinDir="${PROJ_APP_DIR}/${appDir}"
deploy
startApp
