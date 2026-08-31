#! /bin/bash
set -euo pipefail

./build.sh clean install -d "${1}"
