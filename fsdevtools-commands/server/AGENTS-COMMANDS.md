# Server Commands

## CLI Usage

```
fs-cli server start [options]
fs-cli server stop [options]
```

## Commands

| Subcommand | Description |
|---|---|
| `start` | Start a local FirstSpirit server instance |
| `stop` | Stop a running FirstSpirit server instance |

## Key Classes

| Class | Role |
|---|---|
| `ServerCommandGroup` | `@Group(name = "server")` — group declaration |
| `cmd/start/ServerStartCommand` | Starts the FirstSpirit server |
| `cmd/stop/ServerStopCommand` | Stops the FirstSpirit server |

The actual server lifecycle logic lives in `fsdevtools-serverrunner`, which is a dependency of `fsdevtools-cli` (not directly of this module).
