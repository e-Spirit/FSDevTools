# Project Commands

## CLI Usage

```
fs-cli project <subcommand> [options]
```

## Commands

| Subcommand | Description |
|---|---|
| `export` | Export a FirstSpirit project to a local archive |
| `import` | Import a project archive into a FirstSpirit server |
| `delete` | Delete a FirstSpirit project |
| `activate-webserver` | Activate or configure the web server for a project |

## Key Classes

| Class | Role |
|---|---|
| `ProjectCommandGroup` | `@Group(name = "project")` — group declaration |
| `cmd/export/ProjectExportCommand` | Exports a project |
| `cmd/importproject/ProjectImportCommand` | Imports a project |
| `cmd/delete/ProjectDeleteCommand` | Deletes a project |
| `cmd/activatewebserver/ActivateWebServerCommand` | Activates project web server |

## Notes

Export and import commands may involve large data transfers. JSON schemas for export/import parameters are located in the `json-schema/` directory and are bundled into the distribution under `docs/json-schema/`.
