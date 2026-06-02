# Build & Test

## Build

```bash
./gradlew :fsdevtools-cli:build
./gradlew :fsdevtools-cli:test
```

Dependencies: `fsdevtools-cli-api`, `fsdevtools-common`, `fsdevtools-commands`, `fsdevtools-serverrunner`, `fsdevtools-sharedutils`, `classgraph`, `log4j-core`, `log4j-slf4j2-impl`.

Publishing is **disabled** for this module (`disablePublishing()`).

## Test Notes

Tests require extra JVM flags:
```
--add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED
--add-opens=java.base/java.util=ALL-UNNAMED
```

These are configured in `build.gradle.kts` via `tasks.test { jvmArgs(...) }`. If tests throw `InaccessibleObjectException`, check these flags first.
