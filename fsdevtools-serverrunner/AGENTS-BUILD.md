# Build & Integration Test Context

## Dependencies

- `fsdevtools-sharedutils`
- `org.apache.logging.log4j:log4j-slf4j2-impl` (runtime, for tests)
- `fs-isolated-runtime` (compile-only — FirstSpirit API)

## Build

```bash
./gradlew :fsdevtools-serverrunner:build
./gradlew :fsdevtools-serverrunner:test
```

## Integration Test Context

The root `build.gradle.kts` allocates two random free ports at build time for integration tests:

```kotlin
extra["serverHttpPort"]   = ports[0]   // HTTP port for embedded server
extra["serverSocketPort"] = ports[1]   // Socket port for embedded server
```

These are accessible inside tests via system properties. Ports are chosen in the ephemeral range (49152–65535) to avoid conflicts.
