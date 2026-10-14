# Classloading Architecture

## Compile vs. runtime scopes

| Scope | What it applies to | Rule |
|---|---|---|
| `compileOnly` | `fs-isolated-runtime` in all Java modules | FirstSpirit API must be resolved from the server/CLI classpath at runtime. Never bundle it. |
| `implementation` (bundled) | All other deps in command modules | Bundled into the uber JAR via ShadowJar. |
| `compileOnly` in plugin JARs | `fsdevtools-cli-api` in script engine / custom command plugins | The CLI provides the API at runtime — do not bundle it. |

## Plugin classloader (parent-first)

The CLI loads each script engine JAR from `plugins/` through a **parent-first classloader**. Custom
command JARs belong into `lib/` (commands are found on the classpath). Most CLI libraries
are **shaded** under `com.espirit.moddev.cli.shaded.*` and are therefore invisible to plugins —
plugins may safely bundle their own Jackson, Guava, or Commons without conflict.

The following are **not** shaded and are shared via the parent classloader:

- All CLI classes and `fs-isolated-runtime` classes
- `com.github.rvesse:airline` — plugin commands are annotated with its classes
  (`@Command`, `@Group`, `@Examples`), so the CLI and the plugins must share them. A plugin's
  own Airline copy is ignored (parent-first); commands must use the CLI's Airline version.
- `org.slf4j:slf4j-api` (logging bridge — must be shared for log routing to work)
- `org.apache.logging.log4j:log4j-core` / `log4j-slf4j2-impl`

**Consequence:** Plugin JARs only need to avoid bundling `slf4j-api`, Log4j and Airline — everything
else can be bundled freely.

**Jackson stays invisible to plugins (requirement).** The shaded jar must contain no class of a relocated
library under its original package; `verifyShadowJar` enforces it. Consequently Jackson annotations on a
plugin's own result classes (`@JsonSerialize`, `@JsonProperty`) are not loadable and have no effect: the JVM drops
them, and a custom command gets **no JSON result file** (`-rf`). `Cli` detects this by reading the result class file
(not by reflection) and logs a warning naming the class. There is no plugin-side replacement; the warning shows
whether anyone relies on it. Pinned by `ClassloadingTest` (C8b, C10).
Do not write the shaded package names as string literals in CLI code: the shadow plugin rewrites them.

## Service files (`META-INF/services`)

`ServiceLoader` derives the file name from the interface's runtime name, so a relocated library
needs its service files renamed and rewritten as well. `tasks.shadowJar` calls `mergeServiceFiles()`
for that, and `verifyShadowJar` (runs after `shadowJar` and with `check`) fails the build if the jar still holds a
class of a relocated package, or if any
service file in the jar is named after a relocated package, or lists a class that is not in the jar
or lies in a relocated package. When relocating a new library, add it to the `relocations` map in
the root `build.gradle.kts`; the guard reads its package list from there.

Do **not** relocate a library whose classes appear in plugin signatures or annotations (Airline).

## Classloading tests

`fsdevtools-classloading-tests` starts the real distribution layout (`lib/*` classpath, same JVM
flags as `fs-cli.sh`) in separate JVMs and varies only the jars on disk. Run it with
`./gradlew :fsdevtools-classloading-tests:classloadingTest` (part of `check`, not of `test`).
`-PbaselineJar=<path>` runs the same suite against another CLI jar, e.g. the published 4.8.9 jar,
where cases C1, C2, C3, C6, C7 and C8b must fail. The existing unit tests run unshaded and cannot see
this bug class.
