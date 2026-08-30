#!/bin/bash

### Get the project specific vars
. ../proj_specific_vars.sh

# START
if [ $# -eq 0 ];then
    echo "Maven project directory name is not provided"
    echo "  The script accepts 2 types of parameters: 1)maven commands, 2)dir:<mvnProjDir>"
    echo "    1) maven commands: The complete maven command. Ex: 'clean package', 'clean package -DskipTests', 'dependency:go-offline -B' etc"
    echo "    2) Param 'dir:<mvnProjDir>': The maven project directory name where pom.xml is present or the sub directory path with the last element of the path as the maven project directory"
    exit 1
fi

for param in "$@";do
  case "${param}" in

  *)
    if [ -z "${mvnAppSubDir}" ] && [ "$(echo "${param}" | cut -c1-4)" == "dir:" ];then
      mvnAppSubDir="$(echo "${param}" | cut -c5-)"
    else
      mvnCmd="${mvnCmd} ${param}"
    fi
  ;;

  esac
done

if [ -z "${mvnAppSubDir}" ];then
  echo "ERROR: Directory containing the maven project is not provided"
  exit 1
elif [ -z "${mvnCmd}" ];then
    echo "ERROR: Maven command is not provided. Ex: 'clean package', 'clean package -DskipTests', 'dependency:go-offline -B' etc"
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

/bin/bash -c "./mvn_build_scripts/build_mod.sh \"${mvnAppSubDir}\" \"${mvnAppDir}\" \"${mvnAppSpecificPropertyFile}\" \"${mvnCmd}\""
