# Classloading Architecture

## Compile vs. runtime scopes

| Scope | What it applies to | Rule |
|---|---|---|
| `compileOnly` | `fs-isolated-runtime` in all Java modules | FirstSpirit API must be resolved from the server/CLI classpath at runtime. Never bundle it. |
| `implementation` (bundled) | All other deps in command modules | Bundled into the uber JAR via ShadowJar. |
| `compileOnly` in plugin JARs | `fsdevtools-cli-api` in script engine / custom command plugins | The CLI provides the API at runtime — do not bundle it. |

## Plugin classloader (parent-first)

The CLI loads each JAR from `plugins/` through a **parent-first classloader** — the plugin sees the CLI's copies of shared libraries first. Plugin JARs **cannot override** any of the following (always inherited from the CLI):

- All CLI classes and `fs-isolated-runtime` classes
- `com.fasterxml.jackson.core:jackson-databind`
- `org.slf4j:slf4j-api`
- `com.google.guava:guava`
- `org.apache.commons:commons-compress` / `commons-lang3`
- `org.apache.logging.log4j:log4j-core` / `log4j-slf4j2-impl`
- `com.github.rvesse:airline`

**Consequence:** Script engine plugins and custom command plugins must bundle **only** their own implementation classes and unique transitive dependencies not already in that list.
