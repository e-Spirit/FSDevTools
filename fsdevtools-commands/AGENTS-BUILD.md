# Build & Adding Command Groups

## Build

```bash
# Build all command modules
./gradlew :fsdevtools-commands:build

# Build a specific submodule
./gradlew :fsdevtools-commands:feature:build
./gradlew :fsdevtools-commands:project:build
```

## Adding a New Command Group

1. Create a new subdirectory under `fsdevtools-commands/`
2. Add a `build.gradle.kts` with the standard dependencies
3. Register the subproject in `settings.gradle.kts` with `include("fsdevtools-commands:mynewgroup")`
4. Add the new submodule as a dependency of `fsdevtools-commands` (or declare it as a direct dependency of `fsdevtools-cli`)
5. Annotate the group class with `@Group` and command classes with `@Command` — the CLI discovers them via classpath scanning
