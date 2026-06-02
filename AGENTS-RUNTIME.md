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
    fs-isolated-runtime.jar             ← ⚠ NOT included — must be placed here manually
  plugins/             ← Drop script engine JARs or custom command JARs here
  docs/
    json-schema/       ← JSON schemas for command inputs
    (Vue.js UI)        ← Interactive command documentation
```

---

## Runtime Prerequisites & Environment Variables

The binary distribution does **not** bundle `fs-isolated-runtime.jar`. It must be placed in `lib/` manually before running.

| Variable | Description | Default |
|---|---|---|
| `fshost` | FirstSpirit host | `localhost` |
| `fsport` | FirstSpirit port | `8000` |
| `fsmode` | Connection mode: `HTTP`, `HTTPS`, `SOCKET` | `HTTP` |
| `fsuser` | Username | `Admin` |
| `fspwd` | Password | `Admin` |
| `fsservletzone` | Servlet zone | — |
| `fsproject` | Project name | *(required for project-scoped commands)* |

All variables can be overridden per-invocation with CLI flags.
