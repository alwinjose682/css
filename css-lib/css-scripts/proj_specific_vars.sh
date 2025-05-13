#!/bin/bash

# Function to resolve the script path
# https://stackoverflow.com/questions/59895/how-do-i-get-the-directory-where-a-bash-script-is-located-from-within-the-script
get_script_dir() {
SOURCE=${BASH_SOURCE[0]} # Note: $0 and ${BASH_SOURCE[0]} are different
while [ -L "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR=$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )
  SOURCE=$(readlink "$SOURCE")
  [[ $SOURCE != /* ]] && SOURCE=$DIR/$SOURCE # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR=$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )
echo $DIR
}

# START
#PROJ_DIR=$(dirname "$(readlink -f "$0")") # Note: $0 and ${BASH_SOURCE[0]} are different
this_script_dir="$(get_script_dir)"
PROJ_DIR="$(dirname "$(dirname "${this_script_dir}")")"
echo "${PROJ_DIR}"
projCfgDir="${PROJ_DIR}/css-config"
APP_CFG_DIR_ROOT="${projCfgDir}/app"
certDirRoot="${projCfgDir}/cert"
projScriptCfgFileName="proj_env.cfg"
projScriptCfgFile="${projCfgDir}/${projScriptCfgFileName}" # 'projScriptCfgFile' is common for all modules

if [ ! -f "${projScriptCfgFile}" ];then
  echo "File named ${projScriptCfgFile} is missing"
  exit 1
fi

#projName=$(sed -n 1p "${projScriptCfgFile}")
CONFIG_STAGE=$(sed -n 2p "${projScriptCfgFile}")
PROJ_PROFILES=$(sed -n 3p "${projScriptCfgFile}")
#MVN_BUILD_SCRIPTS_DIR="${this_script_dir}/"
PROJ_LOG_DIR="${PROJ_DIR}/logs/${CONFIG_STAGE}"
CERT_DIR="${certDirRoot}/${CONFIG_STAGE}"

export CONFIG_STAGE # 'projScriptCfgFile -> CONFIG_STAGE' is common for all modules
export PROJ_PROFILES # 'projScriptCfgFile -> PROJ_PROFILES' is common for all modules
export PROJ_DIR
export APP_CFG_DIR_ROOT
export CERT_DIR
#export MVN_BUILD_SCRIPTS_DIR
export PROJ_LOG_DIR
