#!/bin/bash
set -euo pipefail

function create_cssConfig_volume(){
  # Create a named volume for storing css app config data. Remove the existing volume if any
  # This volume will be shared as read-only with all the css apps
  ${containerEngine} volume rm css-config 1>/dev/null 2>&1 || true && ${containerEngine} volume create css-config 1>/dev/null 2>&1

  # Copy the 'css-config/app' directory to the volume
  # Certificates are NOT copied to the volume.
  ${containerEngine} run --rm \
    -v css-config:/config:rw \
    -v ${PROJ_CFG_DIR}/app:/app-config:ro \
    alpine cp -r /app-config/. /config/ \
    1>/dev/null  2>&1

  # Give permission
  ${containerEngine} run --rm \
    -v css-config:/config:rw \
    -u 0 \
    alpine sh -c "chmod -R a+rX /config"

  # Verify permission if needed
  # ${containerEngine} run --rm -v css-config:/config:ro alpine ls -la /config
}

# Main
. ../01_common_env.sh
. ../detect_container_engine.sh

# 1. Create a volume with css app config data
create_cssConfig_volume

# exit successfully
exit 0
