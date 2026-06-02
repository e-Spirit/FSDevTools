# Build & Dependencies

## Dependencies

```kotlin
implementation(project(":fsdevtools-cli-api"))
implementation(project(":fsdevtools-common"))
```

`fs-isolated-runtime` is `compileOnly` (provided by root project at runtime).

## Build

```bash
./gradlew :fsdevtools-commands:feature:build
./gradlew :fsdevtools-commands:feature:test
```
