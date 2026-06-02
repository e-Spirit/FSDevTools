# Test Commands

## CLI Usage

```
fs-cli test <subcommand> [options]
```

## Commands

| Subcommand | Description |
|---|---|
| `connection` | Test that a connection to the FirstSpirit server can be established |
| `project` | Test that a specific project is accessible on the server |

## Key Classes

| Class | Role |
|---|---|
| `TestCommandGroup` | `@Group(name = "test")` — group declaration |
| `cmd/connectiontest/ConnectionTestCommand` | Tests server connectivity |
| `cmd/projecttest/ProjectTestCommand` | Tests project accessibility |

## CI Usage Pattern

```bash
# Verify server is ready before running other commands
fs-cli -h myserver -p 8000 test connection
fs-cli -h myserver -p 8000 -r myproject test project
```
