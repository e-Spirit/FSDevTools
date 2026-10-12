@echo off
set java_cmd=%JAVA_HOME%\bin\java
set javap_cmd=%JAVA_HOME%\bin\javap
set script_path=%~dp0
set CLI_DIR=%script_path:~0,-4%

REM MODE "download": fetches fs-isolated-runtime.jar from a FirstSpirit server into lib\
REM Runs before JAVA_HOME is used, so it also works on a fresh distribution without a runtime jar.
if /i "%~1"=="download" goto :download

REM DETERMINE JAVA CLASS VERSION
for /f "tokens=*" %%a in ('call "%javap_cmd%" -verbose java.lang.Object ^| findstr major') do set BYTECODE_VERSION=%%a
for /f "tokens=3 delims= " %%A in ("%BYTECODE_VERSION%") do set /a BYTECODE_VERSION=%%A
set VM_ARGS=
if %BYTECODE_VERSION% gtr 52  (
  set VM_ARGS=--add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED
)

REM EXECUTE CLI
call "%java_cmd%" %VM_ARGS% -Xmx512m -Dlog4j.configurationFile="%CLI_DIR%conf/log4j2.xml" -cp "%CLI_DIR%\lib\*" com.espirit.moddev.cli.Main %*
exit /b %ERRORLEVEL%

:download
REM Delayed expansion stays off while the arguments are read, so "!" in a password survives.
setlocal EnableExtensions DisableDelayedExpansion
set "DL_P1="
set "DL_P2="
set "DL_P3="
set "DL_URL="
set "DL_USER="
set "DL_PWD="
set "DL_FORCE="
set "DL_STARTED="
set "DL_N=0"
set "DL_LIB=%CLI_DIR%lib"
set "DL_TMP=%CLI_DIR%lib\fs-isolated-runtime.jar.download"
set "DL_VFILE=%TEMP%\fs-cli-version-%RANDOM%.tmp"
set "DL_CFILE=%TEMP%\fs-cli-code-%RANDOM%.tmp"
shift

:dl_parse
if "%~1"=="" goto :dl_parsed
if /i "%~1"=="--force" goto :dl_flag_force
set "DL_ARG=%~1"
if "%DL_ARG:~0,2%"=="--" goto :dl_err_option
set /a DL_N+=1
if %DL_N% gtr 3 goto :dl_err_args
if %DL_N%==1 set "DL_P1=%~1"
if %DL_N%==2 set "DL_P2=%~1"
if %DL_N%==3 set "DL_P3=%~1"
shift
goto :dl_parse

:dl_flag_force
set "DL_FORCE=1"
shift
goto :dl_parse

:dl_parsed
set "DL_URL=%DL_P1%"
set "DL_USER=%DL_P2%"
set "DL_PWD=%DL_P3%"
REM a first argument without scheme is no url: with fsurl set, the url comes from fsurl (http:// is
REM prepended if it has no scheme) and the arguments are ^<user^> then ^<password^>
if not defined fsurl goto :dl_args_set
if not defined DL_P1 goto :dl_no_scheme
if /i "%DL_P1:~0,7%"=="http://" goto :dl_args_set
if /i "%DL_P1:~0,8%"=="https://" goto :dl_args_set
:dl_no_scheme
if %DL_N% gtr 2 goto :dl_err_args
set "DL_USER=%DL_P1%"
set "DL_PWD=%DL_P2%"
set "DL_URL=%fsurl%"
if /i "%DL_URL:~0,7%"=="http://" goto :dl_args_set
if /i "%DL_URL:~0,8%"=="https://" goto :dl_args_set
set "DL_URL=http://%fsurl%"
:dl_args_set
if not defined DL_USER set "DL_USER=%fsuser%"
if not defined DL_PWD set "DL_PWD=%fspwd%"
setlocal EnableDelayedExpansion
if "!DL_URL:~-1!"=="/" set "DL_URL=!DL_URL:~0,-1!"
if /i not "!DL_URL:~0,7!"=="http://" if /i not "!DL_URL:~0,8!"=="https://" goto :dl_err_url
if /i "!DL_URL:~0,7!"=="http://" if "!DL_URL:~7,1!"=="" goto :dl_err_url
if /i "!DL_URL:~0,8!"=="https://" if "!DL_URL:~8,1!"=="" goto :dl_err_url
if not defined DL_USER goto :dl_err_user
if not defined DL_PWD goto :dl_err_pwd

where curl >nul 2>&1
if errorlevel 1 goto :dl_err_curl
if not exist "!DL_LIB!\" goto :dl_err_lib

REM server version, e.g. 2026.8.1.abc1234
set "DL_HTTP="
curl --silent --show-error --max-redirs 0 --connect-timeout 60 -u "!DL_USER!:!DL_PWD!" -o "!DL_VFILE!" -w "%%{http_code}" "!DL_URL!/version.txt" >"!DL_CFILE!"
set "DL_RC=!ERRORLEVEL!"
if not "!DL_RC!"=="0" goto :dl_err_unreachable
set /p DL_HTTP=<"!DL_CFILE!"
if not "!DL_HTTP!"=="200" goto :dl_err_noversion
set "DL_VER="
if exist "!DL_VFILE!" set /p DL_VER=<"!DL_VFILE!"
REM drop leading spaces (trailing ones are accepted by the check below)
:dl_trim
if defined DL_VER if "!DL_VER:~0,1!"==" " set "DL_VER=!DL_VER:~1!" & goto :dl_trim
if not defined DL_VER goto :dl_err_noversion
REM findstr /x needs a line ending, which the server may omit, so write the first line back with one
<nul set /p "=!DL_VER!" >"!DL_VFILE!"
echo.>>"!DL_VFILE!"
findstr /v /r /x /c:"[A-Za-z0-9._-][A-Za-z0-9._-]* *" "!DL_VFILE!" >nul
if not errorlevel 1 goto :dl_err_badversion
for /f "tokens=1" %%v in ("!DL_VER!") do set "DL_VER=%%v"
if not "!DL_VER:~64,1!"=="" goto :dl_err_badversion

REM lib\: same version present means up to date, unless --force; any other runtime jar is replaced
set "DL_TARGET=!DL_LIB!\fs-isolated-runtime-!DL_VER!.jar"
if not defined DL_FORCE if exist "!DL_TARGET!" goto :dl_uptodate

REM fetch into a name the lib\* wildcard ignores, so an aborted download never reaches the classpath
>&2 echo Downloading !DL_URL!/jar/fs-isolated-runtime.jar ...
set "DL_STARTED=1"
set "DL_HTTP="
curl --progress-bar --show-error --max-redirs 0 --connect-timeout 60 -u "!DL_USER!:!DL_PWD!" -o "!DL_TMP!" -w "%%{http_code}" "!DL_URL!/jar/fs-isolated-runtime.jar" >"!DL_CFILE!"
set "DL_RC=!ERRORLEVEL!"
if not "!DL_RC!"=="0" goto :dl_err_unreachable
set /p DL_HTTP=<"!DL_CFILE!"
if "!DL_HTTP!"=="401" goto :dl_err_auth
if "!DL_HTTP!"=="404" goto :dl_err_endpoint
if "!DL_HTTP:~0,1!"=="3" goto :dl_err_redirect
if not "!DL_HTTP!"=="200" goto :dl_err_response
set "DL_SIZE=0"
if exist "!DL_TMP!" for %%F in ("!DL_TMP!") do set "DL_SIZE=%%~zF"
if "!DL_SIZE!"=="0" goto :dl_err_response
findstr /b /c:"PK" "!DL_TMP!" >nul
if errorlevel 1 goto :dl_err_response

for %%F in ("!DL_LIB!\fs-isolated-runtime*.jar") do if /i "%%~xF"==".jar" del /q "%%F" & echo Removed %%~nxF
move /y "!DL_TMP!" "!DL_TARGET!" >nul
if errorlevel 1 goto :dl_err_write
for %%F in ("!DL_TARGET!") do set "DL_SIZE=%%~zF"
echo Downloaded fs-isolated-runtime-!DL_VER!.jar (!DL_SIZE! bytes) to !DL_TARGET!
set "DL_EXIT=0"
goto :dl_end

:dl_uptodate
echo fs-isolated-runtime-!DL_VER!.jar is already up to date. Nothing downloaded.
set "DL_EXIT=0"
goto :dl_end

:dl_err_option
>&2 echo Unknown option.
goto :dl_usage
:dl_err_args
>&2 echo Too many arguments.
goto :dl_usage
:dl_err_url
>&2 echo The url must start with http:// or https://: pass it as first argument or set the environment variable fsurl.
goto :dl_usage
:dl_err_user
>&2 echo The user is missing: pass it as second argument or set the environment variable fsuser.
goto :dl_usage
:dl_err_pwd
>&2 echo The password is missing: pass it as third argument or set the environment variable fspwd.
goto :dl_usage
:dl_usage
>&2 echo Usage: fs-cli download [^<url^>] [^<user^>] [^<password^>] [--force]
>&2 echo   ^<url^>       FirstSpirit server base URL, e.g. http://localhost:8000 ^(optional if fsurl is set^)
>&2 echo   ^<user^>      optional if the environment variable fsuser is set
>&2 echo   ^<password^>  optional if the environment variable fspwd is set
>&2 echo   --force     download even if lib\ already contains the jar of the server version
goto :dl_fail

:dl_err_curl
>&2 echo curl is required for "download" and was not found on the PATH.
goto :dl_fail
:dl_err_lib
>&2 echo Cannot write to !DL_LIB!.
goto :dl_fail
:dl_err_write
call :dl_nl
>&2 echo Cannot write to !DL_LIB!.
goto :dl_fail
:dl_err_unreachable
call :dl_nl
>&2 echo Could not reach !DL_URL! (curl exit !DL_RC!). TLS errors: no --insecure option is offered.
goto :dl_fail
:dl_err_noversion
>&2 echo Could not read the server version from !DL_URL!/version.txt (HTTP !DL_HTTP!). Check the url.
goto :dl_fail
:dl_err_badversion
>&2 echo Unexpected server version; refusing to use it in a file name.
goto :dl_fail
:dl_err_auth
call :dl_nl
>&2 echo Authentication failed for user !DL_USER!.
goto :dl_fail
:dl_err_endpoint
call :dl_nl
>&2 echo Endpoint not found on the server (/jar/fs-isolated-runtime.jar). Check the url.
goto :dl_fail
:dl_err_redirect
call :dl_nl
>&2 echo The server redirected the request. Check the url.
goto :dl_fail
:dl_err_response
call :dl_nl
>&2 echo Unexpected response (HTTP !DL_HTTP!); nothing was written.
goto :dl_fail

:dl_nl
REM a started download leaves the cursor inside the progress bar, so start on a new line
if defined DL_STARTED >&2 echo.
exit /b 0

:dl_fail
set "DL_EXIT=1"

:dl_end
del /q "%DL_VFILE%" "%DL_CFILE%" "%DL_TMP%" >nul 2>&1
exit /b %DL_EXIT%
