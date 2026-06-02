# Service Commands

## CLI Usage

```
fs-cli service <subcommand> [options]
```

## Commands

| Subcommand | Description |
|---|---|
| `list` | List all services registered on the FirstSpirit server |
| `restart` | Restart a specific service by name |

## Key Classes

| Class | Role |
|---|---|
| `ServiceCommandGroup` | `@Group(name = "service")` — group declaration |
| `cmd/list/ServiceListCommand` | Lists all services |
| `cmd/restart/ServiceRestartCommand` | Restarts a named service |
