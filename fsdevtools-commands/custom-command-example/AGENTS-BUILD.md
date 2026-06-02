# Build & Dependencies

## Dependencies

```kotlin
// Only depends on the API, not the full CLI
implementation(project(":fsdevtools-cli-api"))
```

## Build

```bash
./gradlew :fsdevtools-commands:custom-command-example:build
```
