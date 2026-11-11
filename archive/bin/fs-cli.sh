#!/bin/sh
JAVACMD="$JAVA_HOME/bin/java";

# Tries to use readlink or realpath. If not installed, falls back to no conversion.
my_realpath() {
    readlink -f "$1" 2>/dev/null || realpath "$1" 2>/dev/null || echo "$1"
}

FS_CLI_DIR="$( cd "$(dirname "$(my_realpath "$0")")/../" ; pwd -P )/"

# Mode "download": fetches fs-isolated-runtime.jar from a FirstSpirit server into lib/.
# Runs before JAVA_HOME is used, so it also works on a fresh distribution without a runtime jar.
fs_cli_download_started=""
fs_cli_download_error() {
    # a started download leaves the cursor inside the progress bar, so start on a new line
    [ -n "${fs_cli_download_started}" ] && printf '\n' >&2
    printf '%s\n' "$*" >&2
}

fs_cli_download_usage() {
    fs_cli_download_error "$1"
    printf '%s\n' 'Usage: fs-cli download [<url>] [<user>] [<password>] [--force]' >&2
    printf '%s\n' '  <url>       FirstSpirit server base URL, e.g. http://localhost:8000; optional if the environment variable fsurl is set' >&2
    printf '%s\n' '  <user>      optional if the environment variable fsuser is set' >&2
    printf '%s\n' '  <password>  optional if the environment variable fspwd is set' >&2
    printf '%s\n' '  --force     download even if lib/ already contains the jar of the server version' >&2
}

fs_cli_download() {
    shift
    dl_force=""; dl_url=""; dl_user=""; dl_password=""; dl_count=0
    for dl_arg in "$@"; do
        case "${dl_arg}" in
            --force) dl_force=1 ;;
            --*) fs_cli_download_usage "Unknown option: ${dl_arg}"; return 1 ;;
            *)
                dl_count=$((dl_count + 1))
                case ${dl_count} in
                    1) dl_url="${dl_arg}" ;;
                    2) dl_user="${dl_arg}" ;;
                    3) dl_password="${dl_arg}" ;;
                    *) fs_cli_download_usage "Too many arguments."; return 1 ;;
                esac
                ;;
        esac
    done
    # a first argument without scheme is no url: with fsurl set, the url comes from fsurl (http:// is
    # prepended if it has no scheme) and the arguments are <user> [<password>]
    if [ -n "${fsurl}" ]; then
        case "${dl_url}" in
            [Hh][Tt][Tt][Pp]://*|[Hh][Tt][Tt][Pp][Ss]://*) ;;
            *)
                if [ ${dl_count} -gt 2 ]; then fs_cli_download_usage "Too many arguments."; return 1; fi
                dl_password="${dl_user}"; dl_user="${dl_url}"
                dl_url="${fsurl}"
                case "${dl_url}" in [Hh][Tt][Tt][Pp]://*|[Hh][Tt][Tt][Pp][Ss]://*) ;; *) dl_url="http://${dl_url}" ;; esac
                ;;
        esac
    fi
    [ -z "${dl_user}" ] && dl_user="${fsuser}"
    [ -z "${dl_password}" ] && dl_password="${fspwd}"
    dl_url="${dl_url%/}"
    case "${dl_url}" in
        [Hh][Tt][Tt][Pp]://?*|[Hh][Tt][Tt][Pp][Ss]://?*) ;;
        *) fs_cli_download_usage "The url must start with http:// or https://: pass it as first argument or set the environment variable fsurl."; return 1 ;;
    esac
    if [ -z "${dl_user}" ]; then
        fs_cli_download_usage "The user is missing: pass it as second argument or set the environment variable fsuser."; return 1
    fi
    if [ -z "${dl_password}" ]; then
        fs_cli_download_usage "The password is missing: pass it as third argument or set the environment variable fspwd."; return 1
    fi

    if ! command -v curl >/dev/null 2>&1; then
        fs_cli_download_error 'curl is required for "download" and was not found on the PATH.'; return 1
    fi
    dl_lib="${FS_CLI_DIR}lib"
    if [ ! -d "${dl_lib}" ] || [ ! -w "${dl_lib}" ]; then
        fs_cli_download_error "Cannot write to ${dl_lib}."; return 1
    fi

    # server version, e.g. 2026.8.1.abc1234
    dl_nl='
'
    dl_response=$(curl --silent --show-error --max-redirs 0 --connect-timeout 60 -u "${dl_user}:${dl_password}" -w '\n%{http_code}' "${dl_url}/version.txt")
    dl_rc=$?
    if [ ${dl_rc} -ne 0 ]; then
        fs_cli_download_error "Could not reach ${dl_url} (curl exit ${dl_rc}). TLS errors: no --insecure option is offered."; return 1
    fi
    dl_http="${dl_response##*"${dl_nl}"}"
    dl_version=$(printf '%s' "${dl_response%"${dl_nl}"*}" | tr -d ' \t\r\n')
    if [ "${dl_http}" != "200" ] || [ -z "${dl_version}" ]; then
        fs_cli_download_error "Could not read the server version from ${dl_url}/version.txt (HTTP ${dl_http}). Check the url."; return 1
    fi
    case "${dl_version}" in
        *[!A-Za-z0-9._-]*)
            fs_cli_download_error "Unexpected server version \"${dl_version}\"; refusing to use it in a file name."; return 1 ;;
    esac
    if [ ${#dl_version} -gt 64 ]; then
        fs_cli_download_error "Unexpected server version \"${dl_version}\"; refusing to use it in a file name."; return 1
    fi

    # lib/: same version present means up to date, unless --force; any other runtime jar is replaced
    dl_target="${dl_lib}/fs-isolated-runtime-${dl_version}.jar"
    if [ -z "${dl_force}" ] && [ -n "$(find "${dl_lib}" -maxdepth 1 -iname "fs-isolated-runtime-${dl_version}.jar")" ]; then
        printf '%s\n' "fs-isolated-runtime-${dl_version}.jar is already up to date. Nothing downloaded."
        return 0
    fi
    dl_existing=$(find "${dl_lib}" -maxdepth 1 -iname 'fs-isolated-runtime*.jar')

    # fetch into a name the lib/* wildcard ignores, so an aborted download never reaches the classpath
    dl_tmp="${dl_lib}/fs-isolated-runtime.jar.download"
    trap 'rm -f "${dl_tmp}"' EXIT
    trap 'exit 130' INT
    trap 'exit 143' TERM
    printf '%s\n' "Downloading ${dl_url}/jar/fs-isolated-runtime.jar ..." >&2
    fs_cli_download_started=1
    dl_http=$(curl --progress-bar --show-error --max-redirs 0 --connect-timeout 60 -u "${dl_user}:${dl_password}" -o "${dl_tmp}" -w '%{http_code}' "${dl_url}/jar/fs-isolated-runtime.jar")
    dl_rc=$?
    if [ ${dl_rc} -ne 0 ]; then
        fs_cli_download_error "Could not reach ${dl_url} (curl exit ${dl_rc}). TLS errors: no --insecure option is offered."; return 1
    fi
    case "${dl_http}" in
        200) ;;
        401) fs_cli_download_error "Authentication failed for user \"${dl_user}\"."; return 1 ;;
        404) fs_cli_download_error "Endpoint not found on the server (/jar/fs-isolated-runtime.jar). Check the url."; return 1 ;;
        3??) fs_cli_download_error "The server redirected the request. Check the url."; return 1 ;;
        *) fs_cli_download_error "Unexpected response (HTTP ${dl_http}); nothing was written."; return 1 ;;
    esac
    if [ ! -s "${dl_tmp}" ] || [ "$(head -c 2 "${dl_tmp}")" != "PK" ]; then
        fs_cli_download_error "Unexpected response (HTTP ${dl_http}); nothing was written."; return 1
    fi

    if [ -n "${dl_existing}" ]; then
        printf '%s\n' "${dl_existing}" | while IFS= read -r dl_old; do
            rm -f "${dl_old}" && printf '%s\n' "Removed ${dl_old}"
        done
    fi
    if ! mv -f "${dl_tmp}" "${dl_target}"; then
        fs_cli_download_error "Cannot write to ${dl_lib}."; return 1
    fi
    printf '%s\n' "Downloaded fs-isolated-runtime-${dl_version}.jar ($(wc -c < "${dl_target}" | tr -d ' ') bytes) to ${dl_target}"
    return 0
}

if [ "$1" = "download" ]; then
    fs_cli_download "$@"
    exit $?
fi

BYTECODE_VERSION=$("${JAVA_HOME}"/bin/javap -verbose java.lang.Object | grep major | cut -d " " -f5)
if [ "${BYTECODE_VERSION}" -gt 52 ]; then
  VM_ARGS="--add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED"
fi

# SELECT FIRSTSPIRIT RUNTIME JAR
# The api-lib directory may contain one fs-isolated-runtime jar per server version
# (e.g. fs-isolated-runtime-5.2.260513.jar). The newest one is put on the classpath;
# on a version mismatch the cli downloads the matching jar and restarts itself with it.
FS_CLI_CLASSPATH="${FS_CLI_DIR}lib/*"
API_LIB_JAR=$(ls "${FS_CLI_DIR}api-lib/"fs-isolated-runtime-*.jar 2>/dev/null | sort | tail -n 1)
if [ -n "$API_LIB_JAR" ]; then
  FS_CLI_CLASSPATH="${FS_CLI_CLASSPATH}:${API_LIB_JAR}"
  if ls "${FS_CLI_DIR}lib/"fs-isolated-runtime*.jar >/dev/null 2>&1; then
    echo "WARNING: found fs-isolated-runtime jar in 'lib' AND 'api-lib' - the jar in 'lib' takes precedence. Remove it from 'lib' to use the versioned jars from 'api-lib'." >&2
  fi
fi

$JAVACMD $VM_ARGS -Xmx512m -Dlog4j.configurationFile="${FS_CLI_DIR}conf/log4j2.xml" -cp "$FS_CLI_CLASSPATH" com.espirit.moddev.cli.Main "$@";
RETVAL=$?;
exit ${RETVAL};
