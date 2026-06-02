# Feature Commands

## CLI Usage

```
fs-cli feature <subcommand> [options]
```

## Commands

| Subcommand | Description |
|---|---|
| `list` | List features available in a project (default command for the group) |
| `analyze` | Analyze a feature for conflicts or issues before installation |
| `download` | Download a feature archive from a FirstSpirit project |
| `install` | Install a feature into a FirstSpirit project |
| `revision` | Show feature revision information |

## Key Classes

| Class | Role |
|---|---|
| `FeatureCommandGroup` | `@Group(name = "feature")` — group declaration |
| `cmd/list/FeatureListCommand` | Default command; lists available features |
| `cmd/analyze/FeatureAnalyzeCommand` | Analyzes a feature archive |
| `cmd/download/FeatureDownloadCommand` | Downloads a feature archive |
| `cmd/install/FeatureInstallCommand` | Installs a feature |
