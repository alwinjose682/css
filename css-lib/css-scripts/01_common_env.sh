#!/bin/bash
set -euo pipefail

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
PROJ_CFG_DIR="${PROJ_DIR}/css-config"
APP_CFG_DIR_ROOT="${PROJ_CFG_DIR}/app"
#MVN_BUILD_SCRIPTS_DIR="${this_script_dir}/"
PROJ_APP_DIR="${PROJ_DIR}/app"

export PROJ_DIR
export PROJ_CFG_DIR
export APP_CFG_DIR_ROOT
#export MVN_BUILD_SCRIPTS_DIR
export PROJ_APP_DIR
