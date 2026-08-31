#!/bin/bash
set -euo pipefail

### App specific VM args (JVM Args specified in a file)
#### https://docs.oracle.com/en/java/javase/24/docs/specs/man/java.html#java-command-line-argument-files

appCfgDir="${1}"
jvmOpts=()

if [[ ${containerized} == "true" ]]; then
jvmOpts+=("@${APP_CFG_DIR_ROOT}/vmArgs__common_global__containerized")
argFilesForDisplay="${argFilesForDisplay:-} @vmArgs__common_global__containerized, "
fi
jvmOpts+=("@${APP_CFG_DIR_ROOT}/vmArgs__common_global")
argFilesForDisplay="${argFilesForDisplay:-} @vmArgs__common_global"

for argFile in $(cat "${appCfgDir}/javaCmdLineArgFiles" | tr -s '[:blank:]') ; do
  jvmOpts+=("@${appCfgDir}/${argFile}")
  argFilesForDisplay="${argFilesForDisplay}, @${argFile}"
done

jvmOpts+=("-Dspring.config.location=${APP_CFG_DIR_ROOT}/,${appCfgDir}/")
jvmOpts+=("-Dlogging.config=${appCfgDir}/logback-spring.xml")
jvmOpts+=("-Dspring.profiles.active=${PROJ_PROFILES}")
jvmOpts+=("-Dcss.cert.path=${CERT_DIR}")

