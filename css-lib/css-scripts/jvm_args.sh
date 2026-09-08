#!/bin/bash
set -euo pipefail

### App specific VM args (JVM Args specified in a file)
#### https://docs.oracle.com/en/java/javase/24/docs/specs/man/java.html#java-command-line-argument-files

projProfiles="${1}"
appCfgDir="${2}"
appCfgDirRoot="${3}"
appCertDir="${4}"
containerized="${5}"
buildPackUsed="${6}"
jvmArgs=()

jvmArgs+=("@${appCfgDirRoot}/vmArgs__common_global")
argFilesForDisplay="${argFilesForDisplay:-} @vmArgs__common_global"

if [[ ${containerized} == "true" ]]; then
  jvmArgs+=("@${appCfgDirRoot}/vmArgs__common_global__containerized")
  argFilesForDisplay="${argFilesForDisplay:-}, @vmArgs__common_global__containerized"
  if [ "${buildPackUsed}" == "false" ];then
    jvmArgs+=("@${appCfgDirRoot}/vmArgs__common_global__containerized__non_buildpack")
    argFilesForDisplay="${argFilesForDisplay:-}, @vmArgs__common_global__containerized__non_buildpack"
  fi

  # App specific VM args.
  # For a containerized app:
  #     only the vmArgs__common which is mandatory for any app is used by default.
  #     all other vmArgs are treated as optional and must be supplied explicitly. Ex: via podman cli, K8 configMap etc
  #     NOTE: This is the planned preferred way to provide vmArgs going forward. 'javaCmdLineArgFiles' support will be removed
      jvmArgs+=("@${appCfgDir}/vmArgs__common")
      argFilesForDisplay="${argFilesForDisplay}, @vmArgs__common"
else
  # App specific VM args, selects the vmArgs file that are listed in the metadata file: 'javaCmdLineArgFiles'
  # NOTE: This is ***NOT*** the planned preferred way to provide vmArgs going forward. 'javaCmdLineArgFiles' support will be removed
  for argFile in $(cat "${appCfgDir}/javaCmdLineArgFiles" | tr -s '[:blank:]') ; do
    jvmArgs+=("@${appCfgDir}/${argFile}")
    argFilesForDisplay="${argFilesForDisplay}, @${argFile}"
  done
fi

jvmArgs+=("-Dspring.config.location=${appCfgDirRoot}/,${appCfgDir}/")
jvmArgs+=("-Dlogging.config=${appCfgDir}/logback-spring.xml")
jvmArgs+=("-Dspring.profiles.active=${projProfiles}")
jvmArgs+=("-Dcss.cert.path=${appCertDir}")
