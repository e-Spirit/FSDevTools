# Build, Installation & Notes

## Dependencies

```kotlin
compileOnly(project(":fsdevtools-cli-api"))             // provided by the CLI at runtime
implementation("org.apache.groovy:groovy-jsr223:4.0.17") // bundled in the fat JAR
```

## Build

```bash
./gradlew :fsdevtools-scriptengines:Groovy:shadowJar
# → build/shadowJAR/Groovy-<version>.jar
```

The `shadowJar` task produces a fat JAR named `Groovy-<version>.jar`. The root build renames it to `fs-cli-scriptengine-Groovy-<version>.jar` for distribution.

## Installation

```
cp build/shadowJAR/Groovy-<version>.jar <fs-cli-dir>/plugins/
```

## Group

`com.espirit.moddev.fsdevtools.script-engines` (overrides the default group for this module)
