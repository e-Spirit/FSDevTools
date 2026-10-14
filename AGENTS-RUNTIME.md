# Distribution Layout & Runtime

## Distribution Layout

```
fs-cli/
  bin/
    fs-cli.sh          ← Unix launcher
    fs-cli.cmd         ← Windows launcher
  conf/
    log4j2.xml         ← Log4J configuration (no log files by default; edit to enable)
  lib/
    fsdevtools-cli-<version>.jar        ← Uber JAR
    fs-isolated-runtime[-<version>].jar ← ⚠ NOT included — fetch with `fs-cli download` or place manually
  plugins/             ← Drop script engine JARs here. Custom command JARs go into lib/ (commands are found on the classpath)
  docs/
    json-schema/       ← JSON schemas for command inputs
    (Vue.js UI)        ← Interactive command documentation
```

---

## Runtime Prerequisites & Environment Variables

The binary distribution does **not** bundle `fs-isolated-runtime.jar`. It must be placed in `lib/` before running, either manually or with the download mode of the start scripts:

`fs-cli.sh|fs-cli.cmd download [<url>] [<user>] [<password>] [--force]` (url falls back to `fsurl`, user falls back to `fsuser`, password to `fspwd`).

- Implemented in the scripts only (`archive/bin`, curl, no Java), dispatched before `JAVA_HOME` is used, so it works on a fresh distribution.
- Url from the environment: only used if `fsurl` is set and the first argument is not an `http://` or `https://` url; then the positional arguments are `<user> [<password>]`. `fsurl` without a scheme gets `http://` prepended. Without `fsurl`, a missing url is an error.
- Reads the server version from `<url>/version.txt` (HTTP Basic, same credentials), then stops with "already up to date" (exit `0`, nothing deleted) if `lib/fs-isolated-runtime-<version>.jar` exists and `--force` is not given. Otherwise (version mismatch, no runtime jar, or `--force`) it fetches `<url>/jar/fs-isolated-runtime.jar` (HTTP Basic) via a temp file `fs-isolated-runtime.jar.download` (ignored by the `lib/*` wildcard) and moves it to `lib/fs-isolated-runtime-<version>.jar`.
- `--force` always triggers the download, even if the jar of the server version is already in `lib/`.
- Every existing `fs-isolated-runtime*.jar` in `lib/` (including an unversioned `fs-isolated-runtime.jar` and a same-version jar on `--force`) is replaced without needing `--force`: it is deleted only after the new jar was downloaded and validated, right before the move, so a failed download leaves them untouched.
- Exit code `0` on success or already up to date, `1` on every failure. The `.cmd` variant checks status and size, plus the `PK` header via `findstr`.

| Variable | Description | Default |
|---|---|---|
| `fsurl` | Server base URL, used only by `download` (`http://` is prepended if no scheme is given) | — |
| `fshost` | FirstSpirit host | `localhost` |
| `fsport` | FirstSpirit port | `8000` |
| `fsmode` | Connection mode: `HTTP`, `HTTPS`, `SOCKET` | `HTTP` |
| `fsuser` | Username | `Admin` |
| `fspwd` | Password | `Admin` |
| `fsservletzone` | Servlet zone | — |
| `fsproject` | Project name | *(required for project-scoped commands)* |

All variables can be overridden per-invocation with CLI flags.
