# Script Commands

## CLI Usage

```
fs-cli script <subcommand> [options]
fs-cli [-project <name>] script run [--scriptEngine|-se <ENGINE>] --scriptFile|-sf <PATH>
fs-cli [-project <name>] script parse [--scriptEngine|-se <ENGINE>] --scriptFile|-sf <PATH>
```

## Commands

| Subcommand | Description |
|---|---|
| `parse` | Parse (syntax-check) a script without executing it |
| `run` | Execute a script against a connected FirstSpirit server |

## Script Engines

| Engine name | Availability |
|---|---|
| `beanshell` | Built-in (default) |
| `groovy` | Requires `fs-cli-scriptengine-Groovy-*.jar` in `plugins/` |
| `javascript` | Requires `fs-cli-scriptengine-Javascript-*.jar` in `plugins/` |

The engine is selected via `--scriptEngine` / `-se`. Engines are loaded via `ServiceLoader` from the `plugins/` directory using a parent-first classloader.

## Key Classes

| Class | Role |
|---|---|
| `ScriptCommandGroup` | `@Group(name = "script")` — group declaration |
| `cmd/parse/ScriptParseCommand` | Validates script syntax |
| `cmd/run/ScriptRunCommand` | Executes a script |

`ScriptEngine` and `ScriptExecutable` interfaces are defined in `fsdevtools-cli-api`.
