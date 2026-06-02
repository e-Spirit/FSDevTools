# Build, Installation & Notes

## Dependencies

```kotlin
compileOnly(project(":fsdevtools-cli-api"))                // provided by the CLI at runtime
implementation("org.openjdk.nashorn:nashorn-core:15.4")   // bundled in the fat JAR
```

## Build

```bash
./gradlew :fsdevtools-scriptengines:Javascript:shadowJar
# → build/shadowJAR/Javascript-<version>.jar
```

The root build renames it to `fs-cli-scriptengine-Javascript-<version>.jar` for distribution.

## Installation

```
cp build/shadowJAR/Javascript-<version>.jar <fs-cli-dir>/plugins/
```

## Java Compatibility

Nashorn 15.4 supports Java 17. If upgrading to a newer Nashorn release, verify Java version compatibility in the project's `java.sourceCompatibility` setting (currently Java 17).
