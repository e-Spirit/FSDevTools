@echo off
set java_cmd="%JAVA_HOME%/bin/java"
set script_path=%~dp0
set CLI_DIR=%script_path:~0,-4%
set jarfile="%CLI_DIR%\lib\cli-${project.version}.jar"
%java_cmd% -Dlog4j.configuration=file:"%CLI_DIR%conf/log4j.properties" -jar %jarfile% %*
