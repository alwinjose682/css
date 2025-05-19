#!/bin/bash

### Get the project specific vars
. ../proj_specific_vars.sh

# START
if [ $# -eq 0 ];then
    echo "Maven project directory name is not provided"
    echo "  The script accepts 3 parameters: 1)-SBD, 2)mvn:<like -X>, 3)mvnProjDir"
    echo "    1) Param 'mvn:<like -X>': Optional parameter. Any number of maven options can be given"
    echo "    2) Param 'mvnProjDir': The maven project directory name where pom.xml is present or the sub directory path with the last element of the path as the maven project directory"
    exit 1
fi

for param in "$@";do
  case "${param}" in

  *)
    if [ "$(echo "${param}" | cut -c1-4)" == "mvn:" ];then
          declare -r mvnFlag="$(echo "${param}" | cut -c5-)"
    elif [ -z "${mvnAppSubDir}" ];then
      mvnAppSubDir="${param}"
    else
      echo "Incorrect input"
        exit 1
    fi
  ;;

  esac
done

## Strip off all leading "../"
while [[ "${mvnAppSubDir}" == *'../'* ]];do
  mvnAppSubDir=$(echo "${mvnAppSubDir#../}");
done;

## Strip off one succeeding '/'
mvnAppSubDir=$(echo "${mvnAppSubDir%/}");

declare -r mvnAppDir="${PROJ_DIR}/${mvnAppSubDir}"
declare -r mvnAppSpecificPropertyFile="${mvnAppDir}/mvnprops.properties"

/bin/bash -c "./mvn_build_scripts/install_app.sh ${mvnAppSubDir} ${mvnAppDir} ${mvnAppSpecificPropertyFile} ${mvnFlag}"
