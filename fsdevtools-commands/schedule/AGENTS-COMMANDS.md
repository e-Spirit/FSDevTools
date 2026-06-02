# Schedule Commands

## CLI Usage

```
fs-cli schedule <subcommand> [options]
```

## Commands

| Subcommand | Description |
|---|---|
| `list` | List all schedule entries in a project |
| `start` | Start (trigger) a specific schedule entry |

## Key Classes

| Class | Role |
|---|---|
| `ScheduleCommandGroup` | `@Group(name = "schedule")` — group declaration |
| `cmd/list/ScheduleListCommand` | Lists schedule entries |
| `cmd/start/ScheduleStartCommand` | Triggers a schedule entry |
