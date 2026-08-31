#!/bin/bash
set -euo pipefail
# Note: This script can be used to start the app in any environment(prod, uat, local etc)

javaArgs=()
while [[ $# -gt 0 ]]; do
  case "$1" in
    -d|--dir)
      binDir="$2"
      shift 2
      ;;
    -j|--jar)
      appJar="$2"
      shift 2
      ;;
    -i|--info)
      argFilesForDisplay="$2"
      shift 2
      ;;
#    -*)
#      echo "ERROR: Unknown option ${1}" >&2
#      exit 1
#      ;;
    *)
      # Everything else is treated as a JVM Option(ex: -Xmx)
      javaArgs+=("$1")
      shift
      ;;
  esac
done

javaArgs+=("-jar")
javaArgs+=("${binDir}/${appJar}")

echo "\

JAR_FILE      :      ${binDir}/${appJar}
VM_ARG_FILES  :      ${argFilesForDisplay}
JAVA_CMD      :      ${javaArgs[*]}\
"

### Start the app
### It is important to move to the app's bin directory because relative paths specified in java command line argument files are recognized only with respect to the current working directory
echo ""

java -version
/bin/bash -c ' \
set -euo pipefail; \
cd $1 ; \
shift; \
java "$@" \
' "run_jar_sh" ${binDir} "${javaArgs[@]}"
