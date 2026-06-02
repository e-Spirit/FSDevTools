# Build & Dependencies

## Dependencies

```kotlin
implementation(project(":fsdevtools-cli"))
implementation(project(":fsdevtools-cli-api"))
implementation(project(":fsdevtools-common"))
implementation("de.espirit.firstspirit:fs-isolated-runtime:${fsRuntimeVersion}")
implementation(libs.jackson.databind)
implementation(libs.classgraph)
implementation(libs.airline)
implementation(libs.airline.help.markdown)  // Markdown-formatted help extraction
```

## Build

```bash
./gradlew :fsdevtools-docs-generator:build
```

> This subproject is invoked from the root via `./gradlew createDocumentationJson` — see `AGENTS-GENERATOR.md` for details.
