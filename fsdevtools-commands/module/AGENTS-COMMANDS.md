# Module Commands

## CLI Usage

```
fs-cli module <subcommand> [options]
```

## Commands

| Subcommand | Description |
|---|---|
| `install` | Install a single FSM module onto the FirstSpirit server |
| `install-bulk` | Install multiple FSM modules from a directory |
| `configure` | Configure an already-installed module (web components, project components) |

## Key Classes

| Class | Role |
|---|---|
| `ModuleCommandGroup` | `@Group(name = "module")` — group declaration |
| `cmd/install/ModuleInstallCommand` | Installs a single FSM file |
| `cmd/installbulk/ModuleInstallBulkCommand` | Bulk-installs FSM files |
| `cmd/configure/ModuleConfigureCommand` | Configures module components |
