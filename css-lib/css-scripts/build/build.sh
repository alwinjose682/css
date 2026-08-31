#!/bin/bash
set -euo pipefail

mvnAppSubDir="${1}"

### Get the project specific vars
. ../01_proj_vars.sh

# START
if [ $# -eq 0 ];then
    echo "Maven project directory name is not provided"
    echo "  The script accepts 2 types of parameters: 1)maven commands, 2)-d <mvnProjDir>"
    echo "    1) maven commands: The complete maven command. Ex: 'clean package', 'clean package -DskipTests', 'dependency:go-offline -B' etc"
    echo "    2) Param '-d <mvnProjDir>': The maven project directory name where pom.xml is present or the sub directory path with the last element of the path as the maven project directory"
    exit 1
fi

mvnCmd=()
# Parse 1) maven module directory into a variable and 2) maven commands into an array
while [[ $# -gt 0 ]]; do
  case "$1" in
    -d|--dir)
      mvnAppSubDir="$2"
      shift 2
      ;;
    -*)
      echo "ERROR: Unknown option ${1}" >&2
      exit 1
      ;;
    *)
      mvnCmd+=("$1")
      shift
      ;;
  esac
done

if [ -z "${mvnAppSubDir}" ];then
  echo "ERROR: Directory containing the maven project is not provided" >&2
  exit 1
elif [ -z "${mvnCmd[0]}" ];then
    echo "ERROR: Maven command is not provided. Ex: 'clean package', 'clean package -DskipTests', 'dependency:go-offline -B' etc" >&2
    exit 1
fi

## Strip off all leading "../"
while [[ "${mvnAppSubDir}" == *'../'* ]];do
  mvnAppSubDir=$(echo "${mvnAppSubDir#../}");
done;

## Strip off one succeeding '/'
mvnAppSubDir=$(echo "${mvnAppSubDir%/}");

declare -r mvnAppDir="${PROJ_DIR}/${mvnAppSubDir}"
declare -r mvnAppSpecificPropertyFile="${mvnAppDir}/mvnprops.properties"

./mvn_build_scripts/build_mod.sh "${mvnAppSubDir}" "${mvnAppDir}" "${mvnAppSpecificPropertyFile}" "${mvnCmd[@]}"
