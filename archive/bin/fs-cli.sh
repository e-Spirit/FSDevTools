#!/bin/sh
JAVACMD="$JAVA_HOME/bin/java";

# Tries to use readlink or realpath. If not installed, falls back to no conversion.
my_realpath() {
    readlink -f "$1" 2>/dev/null || realpath "$1" 2>/dev/null || echo "$1"
}

FS_CLI_DIR="$( cd "$(dirname "$(my_realpath "$0")")/../" ; pwd -P )/"

BYTECODE_VERSION=$("${JAVA_HOME}"/bin/javap -verbose java.lang.Object | grep major | cut -d " " -f5)
if [ "${BYTECODE_VERSION}" -gt 52 ]; then
  VM_ARGS="--add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED"
fi

$JAVACMD $VM_ARGS -Xmx512m -Dlog4j.configurationFile="${FS_CLI_DIR}conf/log4j2.xml" -cp "${FS_CLI_DIR}/lib/*" com.espirit.moddev.cli.Main "$@";
RETVAL=$?;
exit ${RETVAL};