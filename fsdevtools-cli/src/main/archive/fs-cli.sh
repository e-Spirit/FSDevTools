#!/bin/sh
JAVACMD="$JAVA_HOME/bin/java";
FS_CLI_DIR="$( cd "$(dirname "$0")/../" ; pwd -P )/"
$JAVACMD -Xmx512m -Dlog4j.configuration=file:"${FS_CLI_DIR}conf/log4j.properties" -cp "${FS_CLI_DIR}/lib/*" com.espirit.moddev.cli.Cli "$@";
RETVAL=$?;
exit ${RETVAL};

