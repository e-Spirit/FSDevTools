@echo off
set java_cmd=%JAVA_HOME%\bin\java
set javap_cmd=%JAVA_HOME%\bin\javap
set script_path=%~dp0
set CLI_DIR=%script_path:~0,-4%

REM DETERMINE JAVA CLASS VERSION
for /f "tokens=*" %%a in ('call "%javap_cmd%" -verbose java.lang.Object ^| findstr major') do set BYTECODE_VERSION=%%a
for /f "tokens=3 delims= " %%A in ("%BYTECODE_VERSION%") do set /a BYTECODE_VERSION=%%A
set VM_ARGS=
if %BYTECODE_VERSION% gtr 52  (
  set VM_ARGS=--add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED
)

REM EXECUTE CLI
call "%java_cmd%" %VM_ARGS% -Xmx512m -Dlog4j.configuration=file:"%CLI_DIR%conf/log4j.properties" -cp "%CLI_DIR%\lib\*" com.espirit.moddev.cli.Main %*"
