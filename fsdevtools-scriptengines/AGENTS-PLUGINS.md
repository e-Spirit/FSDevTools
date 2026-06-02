# Script Engine Plugins

## Submodules

| Submodule | Engine name | Underlying library |
|---|---|---|
| `Groovy/` | `groovy` | `org.apache.groovy:groovy-jsr223:4.0.17` |
| `Javascript/` | `javascript` | `org.openjdk.nashorn:nashorn-core:15.4` |

## Distribution

Script engine JARs are released separately from the main distribution and published as GitHub release assets:

```
fs-cli-scriptengine-Groovy-<version>.jar
fs-cli-scriptengine-Javascript-<version>.jar
```

To install: copy the JAR into the `plugins/` directory of the fs-cli installation.

## Adding a New Script Engine

1. Create a new subdirectory under `fsdevtools-scriptengines/`
2. Add `build.gradle.kts` with:
   ```kotlin
   plugins { id("com.gradleup.shadow") }
   dependencies {
       compileOnly(project(":fsdevtools-cli-api"))   // not bundled — provided by CLI
       implementation("<your-engine-library>")       // bundled in the fat JAR
   }
   tasks.shadowJar { archiveFileName.set("${project.name}-${project.version}.jar") }
   ```
3. Register in `settings.gradle.kts`: `include("fsdevtools-scriptengines:MyEngine")`
4. Implement `ScriptEngine` and `ScriptExecutable` from `fsdevtools-cli-api`
5. Use a unique engine name returned by `ScriptEngine.getName()`

## Classloader Constraints

Script engine JARs **must not** bundle classes already provided by the CLI (Jackson, SLF4J, Guava, Commons, etc.). The parent-first classloader will always use the CLI's version. Only bundle the engine library itself and any unique transitive dependencies.
