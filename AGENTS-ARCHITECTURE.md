# Subproject Architecture

> **Each subproject has its own `AGENTS.md`** with module-specific build details, conventions, and traps. This root file is the cross-cutting reference; the per-subproject file is authoritative for detail. **Read the relevant subproject `AGENTS.md` before editing that module.**

| Module | Role | Guide |
|---|---|---|
| `fsdevtools-cli` | Main application entry point — command discovery, CLI parsing, connection lifecycle, uber JAR | → `fsdevtools-cli/AGENTS.md` |
| `fsdevtools-cli-api` | Public API contracts: `Command`, `Config`, `Result`, `ScriptEngine`, `ScriptExecutable`, Airline annotations | → `fsdevtools-cli-api/AGENTS.md` |
| `fsdevtools-sharedutils` | Lowest-level shared utilities — only what `fsdevtools-cli-api` itself needs | → `fsdevtools-sharedutils/AGENTS.md` |
| `fsdevtools-common` | Higher-level shared utilities: file I/O, compression, JSON helpers | → `fsdevtools-common/AGENTS.md` |
| `fsdevtools-serverrunner` | Embedded FirstSpirit server lifecycle management (start/stop/wait) | → `fsdevtools-serverrunner/AGENTS.md` |
| `fsdevtools-commands` | Aggregator — no source, pulls all command modules together | → `fsdevtools-commands/AGENTS.md` |
| `fsdevtools-commands:feature` | `feature` group: analyze, download, install, list, revision | → `fsdevtools-commands/feature/AGENTS.md` |
| `fsdevtools-commands:module` | `module` group: install, install-bulk, configure (FSM files) | → `fsdevtools-commands/module/AGENTS.md` |
| `fsdevtools-commands:project` | `project` group: export, import, delete, activate-webserver | → `fsdevtools-commands/project/AGENTS.md` |
| `fsdevtools-commands:schedule` | `schedule` group: list, start | → `fsdevtools-commands/schedule/AGENTS.md` |
| `fsdevtools-commands:script` | `script` group: parse, run (BeanShell / Groovy / JavaScript) | → `fsdevtools-commands/script/AGENTS.md` |
| `fsdevtools-commands:server` | `server` group: start, stop | → `fsdevtools-commands/server/AGENTS.md` |
| `fsdevtools-commands:service` | `service` group: list, restart | → `fsdevtools-commands/service/AGENTS.md` |
| `fsdevtools-commands:test` | `test` group: connection, project | → `fsdevtools-commands/test/AGENTS.md` |
| `fsdevtools-commands:custom-command-example` | Reference template for building custom command plugins | → `fsdevtools-commands/custom-command-example/AGENTS.md` |
| `fsdevtools-docs` | Vue.js interactive documentation UI (Node 12.18.1, auto-downloaded) | → `fsdevtools-docs/AGENTS.md` |
| `fsdevtools-docs-generator` | Generates `data.json` from CLI command metadata via ClassGraph + Airline | → `fsdevtools-docs-generator/AGENTS.md` |
| `fsdevtools-scriptengines` | Aggregator for optional script engine plugin fat JARs | → `fsdevtools-scriptengines/AGENTS.md` |
| `fsdevtools-scriptengines:Groovy` | Apache Groovy JSR-223 engine plugin | → `fsdevtools-scriptengines/Groovy/AGENTS.md` |
| `fsdevtools-scriptengines:Javascript` | OpenJDK Nashorn engine plugin | → `fsdevtools-scriptengines/Javascript/AGENTS.md` |

## Dependency Graph

```
fsdevtools-cli  (uber JAR — main entry point)
├── fsdevtools-cli-api
│     └── fsdevtools-sharedutils
├── fsdevtools-common
│     └── fsdevtools-cli-api
├── fsdevtools-commands  (aggregator)
│     └── each submodule → fsdevtools-cli-api + fsdevtools-common
├── fsdevtools-serverrunner
│     └── fsdevtools-sharedutils
└── fsdevtools-docs  (compileOnly — Vue.js UI, not bundled)

fsdevtools-docs-generator  → fsdevtools-cli (full classpath, runs as JavaExec — not bundled)
fsdevtools-scriptengines:Groovy / Javascript  → fsdevtools-cli-api (compileOnly only)
```

`fsdevtools-cli-api` depends only on `fsdevtools-sharedutils` — never on `fsdevtools-common`. This keeps the API module lean for third-party custom command authors who take it as a `compileOnly` dependency.
