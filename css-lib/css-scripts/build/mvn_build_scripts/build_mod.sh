#!/bin/bash -x
set -euo pipefail

function setMvnAppSpecificProperties(){
#  ### Custom maven output directory
#  if [ -n "${appBuildDir}" ];then
#    #NOTE: ${project.name}, ${project.version}, ${project.build.finalName} are maven property placeholders that will be expanded by maven during maven execution
#    properties="${properties} -Dproj.build.dir=${appBuildDir}/\$\{project.build.finalName\} "
#  fi
  if [ -f "${mvnAppSpecificPropertyFile}" ];then
    for propItem in $(cat "${mvnAppSpecificPropertyFile}");do
      mvnCmd+=("-D${propItem}")
    done
  fi
}

# Start
declare -r mvnAppSubDir="${1}"
declare -r mvnAppDir="${2}"
declare -r mvnAppSpecificPropertyFile="${3}"
shift 3
declare -r mvnCmd_part2=("${@}")
mvnCmd=()

if [ -d "${mvnAppDir}" ];then
  declare -r pomFile="${mvnAppDir}/pom.xml"

  # Form the maven command
  mvnCmd+=("-f")
  mvnCmd+=("${pomFile}")
  mvnCmd+=("${mvnCmd_part2[@]}")
  setMvnAppSpecificProperties

  echo "MVN_CMD: mvn ${mvnCmd[*]}"
  if [ -f "${pomFile}" ];then
    # Build, by executing mvn with commands
    mvn "${mvnCmd[@]}"
  else
    echo "ERROR: pom.xml is not present in the project directory: ${mvnAppDir}" >&2
    exit 1
  fi
else
  echo "ERROR: Incorrect maven project directory path: ${mvnAppDir}" >&2
  exit 1
fi
