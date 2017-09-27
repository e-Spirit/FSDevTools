@echo off
set java_cmd="%JAVA_HOME%/bin/java"
set script_path=%~dp0
set CLI_DIR=%script_path:~0,-4%
%java_cmd% -Xmx512m -Dlog4j.configuration=file:"%CLI_DIR%conf/log4j.properties" -cp %CLI_DIR%\lib\* com.espirit.moddev.cli.Cli %*
