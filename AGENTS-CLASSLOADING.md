# Classloading Architecture

## Compile vs. runtime scopes

| Scope | What it applies to | Rule |
|---|---|---|
| `compileOnly` | `fs-isolated-runtime` in all Java modules | FirstSpirit API must be resolved from the server/CLI classpath at runtime. Never bundle it. |
| `implementation` (bundled) | All other deps in command modules | Bundled into the uber JAR via ShadowJar. |
| `compileOnly` in plugin JARs | `fsdevtools-cli-api` in script engine / custom command plugins | The CLI provides the API at runtime — do not bundle it. |

## Plugin classloader (parent-first)

The CLI loads each JAR from `plugins/` through a **parent-first classloader**. Most CLI libraries
are **shaded** under `com.espirit.moddev.cli.shaded.*` and are therefore invisible to plugins —
plugins may safely bundle their own Jackson, Guava, Airline, or Commons without conflict.

The following are **not** shaded and are shared via the parent classloader:

- All CLI classes and `fs-isolated-runtime` classes
- `org.slf4j:slf4j-api` (logging bridge — must be shared for log routing to work)
- `org.apache.logging.log4j:log4j-core` / `log4j-slf4j2-impl`

**Consequence:** Plugin JARs only need to avoid bundling `slf4j-api` and Log4j — everything else
can be bundled freely.
