#!/bin/bash

function getProperty(){
  property=""

#  ### Custom maven output directory
#  if [ -n "${appBuildDir}" ];then
#    #NOTE: ${project.name}, ${project.version}, ${project.build.finalName} are maven property placeholders that will be expanded by maven during maven execution
#    property="${property} -Dproj.build.dir=${appBuildDir}/\$\{project.build.finalName\} "
#  fi
  if [ -f "${mvnAppSpecificPropertyFile}" ];then
    for propItem in $(cat "${mvnAppSpecificPropertyFile}");do
      property="${property}-D${propItem} "
#      echo "property:${property}"
    done
  fi

  echo "${property}"
}

# Start
### Mandatory params
declare -r mvnAppSubDir="${1}"
declare -r mvnAppDir="${2}"
declare -r mvnAppSpecificPropertyFile="${3}"
declare -r mvnCmd="${4}"

if [ -d "${mvnAppDir}" ];then
  declare -r pomFile="${mvnAppDir}/pom.xml"
  echo "MVN_CMD: mvn -f ${pomFile} ${mvnCmd}$(getProperty)"
  if [ -f "${pomFile}" ];then
    /bin/bash -c "mvn -f ${pomFile} ${mvnCmd}$(getProperty)"
  else
    echo "ERROR: pom.xml is not present in the project directory: ${mvnAppDir}"
    exit 1
  fi
else
  echo "ERROR: Incorrect maven project directory path: ${mvnAppDir}"
  exit 1
fi
