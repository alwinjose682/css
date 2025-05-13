#!/bin/bash

function startApp(){
  echo ""

  ### JAVA_OPTS
  JAVA_OPTS="-XX:+HeapDumpOnOutOfMemoryError"
  JAVA_OPTS="$JAVA_OPTS -XX:HeapDumpPath=${appLogDir}"

  ### App Specific JVM Args
  vmArgsFile="${appCfgDir}/vmArgs"
  if [ -f "${vmArgsFile}" ];then
    appSpecificVmArgs=$(cat "${vmArgsFile}")
  else
    appSpecificVmArgs=""
  fi

  ### Java cmd
  javacmd="\
java \
$JAVA_OPTS \
-Dapp.log.filePath=${appLogDir} \
-Dspring.config.location=${APP_CFG_DIR_ROOT}/,${appCfgDir}/ \
-Dlogging.config=${APP_CFG_DIR_ROOT}/logback-spring.xml \
-Dspring.profiles.active=${PROJ_PROFILES} \
-Dcss.cert.path=${CERT_DIR} \
${appSpecificVmArgs} \
-jar ${appJar} \
"

echo "\
APP_VM_ARGS   :      ${appSpecificVmArgs}
JAR_FILE      :      ${appJar}
JAVA_CMD      :      ${javacmd}\
"

  ### Start the app
  echo ""
  java -version
  /bin/bash -c " \
cd ${appJarDir} ; \
${javacmd} \
"

}

# START
### Get the project specific vars
. ../proj_specific_vars.sh

appDir="${1}" # Note: 'appDir' is not the complete dir path. Its only the path relative to the project root directory
if [ -z "${appDir}" ];then
  echo "App directory is not provided"
  exit 0
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
CONFIG_STAGE  :      ${CONFIG_STAGE}
PROJ_PROFILES :      ${PROJ_PROFILES}
APP_CFG_DIR   :      ${appCfgDir}
CERT_DIR      :      ${CERT_DIR}\
"

if [ -z "${APP_CFG_DIR_ROOT}" ] || [ -z "${CONFIG_STAGE}" ] || [ -z "${PROJ_LOG_DIR}" ] || [ -z "${CERT_DIR}" ];then
  echo "APP_CFG_DIR_ROOT, CONFIG_STAGE, PROJ_LOG_DIR and CERT_DIR variables must be set"
  exit 1
fi

appJarDir="${PROJ_DIR}/${appDir}/target"
appLogDir="${PROJ_LOG_DIR}/${appDir}"

echo "\
APP_LOG_DIR   :      ${appLogDir}\
"

if [ -d "${appJarDir}" ];then
  appJar=$(find "${appJarDir}" -type f -name "${appDirName}-*-jar-with-dependencies.jar")

  if [ -z "${appJar}" ];then
    jarFileCount=$(find "${appJarDir}" -type f -name "${appDirName}-*.jar" | wc -l)
    if [ ! ${jarFileCount} -eq 1 ];then
      echo "Exactly one jar file, ending with '.jar', should be present in the build directory: ${appJarDir}, count: ${jarFileCount}"
      exit 1
    fi
    appJar=$(find "${appJarDir}" -type f -name "${appDirName}-*.jar")
  fi

  startApp
else
  echo "The app build directory does not exist: ${appJarDir}"
  exit 0
fi
