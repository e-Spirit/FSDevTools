# Public API Contracts

## Key Packages

| Package | Contents |
|---|---|
| `api.command` | `Command<R>` — root interface for all commands (extends `Callable<R>`) |
| `api.configuration` | `Config` — marker + connection configuration interface; commands implementing this get a `ProjectScriptContext` injected |
| `api.result` | `Result<T>` — command result contract; `log()` for output, `isError()`, `getError()` |
| `api.annotations` | `ScheduleTaskCommand` and other Airline-compatible annotations |
| `api.script` | `ScriptEngine`, `ScriptExecutable` — SPI for pluggable script engines |
| `api.parsing` | `Identifier` and parser framework for referencing FirstSpirit entities by UID/name |
| `api.json` | `AttributeNames`, JSON schema support classes |
| `api.event` | CLI event system interfaces |
| `api.validation` | Validation interfaces for command inputs |
| `api.CliContext` | Context object passed to commands; wraps the FS connection |

## Core Contracts

### `Command<R extends Result>`
```java
public interface Command<RESULT_TYPE extends Result> extends Callable<RESULT_TYPE> {}
```
All commands implement this. The `call()` method is the command body.

### `Config`
Commands that need a FirstSpirit connection implement `Config`. The CLI checks `command instanceof Config` to decide whether to establish a connection before executing.

Key methods:
- `needsContext()` — return `false` to skip FS connection (e.g. help commands)
- `setContext(ProjectScriptContext)` — called by the CLI before `call()`
- `getResultFile()` — path for the JSON result output file

### `ScriptEngine` / `ScriptExecutable`
SPI for script engine plugins placed in the `plugins/` directory:
```java
public interface ScriptEngine {
    String getName();                              // unique engine name
    ScriptExecutable getExecutable(String source); // compile the script
}
```
Plugin JARs must bundle their own implementation and dependencies as a fat JAR.

## Extension Points

To add a new command group or command, depend on this module and implement:
1. A class annotated with `@Group` (for the group)
2. One or more classes implementing `Command<R>` annotated with `@com.github.rvesse.airline.annotations.Command`
3. Optionally implement `Config` if the command needs a FirstSpirit connection
