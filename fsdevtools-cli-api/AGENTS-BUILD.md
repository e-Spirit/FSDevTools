# Build & Dependencies

## Dependencies (exposed as `api`)

- `fsdevtools-sharedutils`
- `com.github.rvesse:airline` (2.8.5) — CLI annotation framework
- `com.fasterxml.jackson.core:jackson-databind` (2.18.3) — JSON serialization

Both Airline and Jackson are exposed transitively to all consuming modules via Gradle `api` configuration.

## Build

```bash
./gradlew :fsdevtools-cli-api:build
./gradlew :fsdevtools-cli-api:test
```
