#!/bin/sh
JAVACMD="$JAVA_HOME/bin/java";

# Tries to use readlink or realpath. If not installed, falls back to no conversion.
my_realpath() {
    readlink -f "$1" 2>/dev/null || realpath "$1" 2>/dev/null || echo "$1"
}

FS_CLI_DIR="$( cd "$(dirname "$(my_realpath "$0")")/../" ; pwd -P )/"
$JAVACMD -Xmx512m -Dlog4j.configuration=file:"${FS_CLI_DIR}conf/log4j.properties" -cp "${FS_CLI_DIR}/lib/*" com.espirit.moddev.cli.Main "$@";
RETVAL=$?;
exit ${RETVAL};

