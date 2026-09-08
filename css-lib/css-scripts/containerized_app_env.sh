#!/bin/bash
set -euo pipefail

img="${1}"
appName=$(basename "$(echo ${img} | awk -F":" '{print $1}')")
appRootDir="/css"
cfgDirRoot="${appRootDir}/config"
certDir="${appRootDir}/cert"
cfgDir="${cfgDirRoot}/${appName}"
