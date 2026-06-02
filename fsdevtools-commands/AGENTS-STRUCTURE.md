# Command Group Aggregator — Structure

## Submodules

| Submodule | CLI group name | Description |
|---|---|---|
| `feature/` | `feature` | FirstSpirit feature management (analyze, download, install, list, revision) |
| `module/` | `module` | Module install, configure, bulk-install |
| `project/` | `project` | Project export, import, delete, activate-webserver |
| `schedule/` | `schedule` | Schedule list and start |
| `script/` | `script` | Script parse and run (BeanShell, Groovy, JavaScript) |
| `server/` | `server` | Server start and stop |
| `service/` | `service` | Service list and restart |
| `test/` | `test` | Connection test and project test |
| `custom-command-example/` | `example` | Template/reference for custom command extensions |

## Common Pattern for All Submodules

Every command submodule follows the same structure:

```
src/main/java/com/espirit/moddev/cli/commands/<group>/
  <Group>CommandGroup.java         ← @Group annotation, defines the group name
  cmd/
    <SubCommand>/
      <SubCommand>Command.java     ← @Command annotation, implements Command<R>
      <SubCommand>CommandResult.java  ← result type, often @JsonSerialize
```

All command modules depend on:
```kotlin
implementation(project(":fsdevtools-cli-api"))
implementation(project(":fsdevtools-common"))
```
