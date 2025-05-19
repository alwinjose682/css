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
declare -r mvnAppDirName="${1}"
declare -r mvnAppDir="${2}"
declare -r mvnAppSpecificPropertyFile="${3}"
### Optional Params
if [ -n "${4}" ];then
  declare -r mvnFlag="${4}"
fi

if [ -z "${mvnAppDirName}" ];then
  echo "Maven project directory name is not provided"
  exit 0
fi

if [ -d "${mvnAppDir}" ];then
  declare -r pomFile="${mvnAppDir}/pom.xml"
  echo "MVN_CMD: mvn ${mvnFlag} -f ${pomFile} clean install$(getProperty)"
  if [ -f "${pomFile}" ];then
    /bin/bash -c "mvn ${mvnFlag} -f ${pomFile} clean install$(getProperty)"
  else
    echo "pom.xml is not present in the project directory: ${mvnAppDir}"
    exit 0
  fi
else
  echo "Incorrect maven project directory path: ${mvnAppDir}"
  exit 0
fi
