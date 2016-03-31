#!/bin/sh
JAVACMD="$JAVA_HOME/bin/java";
FS_CLI_DIR="$(dirname $(readlink -f $0))/../";
JARFILE="${FS_CLI_DIR}lib/cli-${project.version}.jar";
$JAVACMD -Dlog4j.configuration=file:"${FS_CLI_DIR}conf/log4j.properties" -jar $JARFILE $@;
RETVAL=$?;
exit ${RETVAL};
