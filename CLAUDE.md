# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FSDevTools (`fs-cli`) is a Java CLI tool for FirstSpirit CMS developer operations — content export/import, module management, project operations, script execution, and server control. Built with Gradle (Kotlin DSL), Java 17+, and the Airline CLI framework.

## Build Commands

```bash
./gradlew build                    # Full build + tests
./gradlew test                     # Run all tests
./gradlew :fsdevtools-cli:test     # Run tests for a specific module
./gradlew test --tests "*TestConnectionCommandTest"           # Run a specific test class
./gradlew test --tests "*TestConnectionCommandTest.testCall"  # Run a specific test method
./gradlew assemble                 # Build tar.gz + zip distributions
./gradlew shadowJar                # Build fat JAR only
./gradlew javadoc                  # Generate Javadoc
./gradlew publishToMavenLocal      # Publish to local Maven cache
```

Use `-PuseLatestFirstSpiritBuild` to compile against FirstSpirit EAP-SNAPSHOT instead of the pinned version.

The FirstSpirit API (`fs-isolated-runtime.jar`) is a `compileOnly` dependency — it must be available via Artifactory (with `-P artifactory_username`/`-P artifactory_password`) or installed in local Maven.

## Module Structure

```
fs-cli (root)                         → Application plugin, shadow JAR, distribution assembly
├── fsdevtools-cli-api                → Public API: Command, Config, Result interfaces, ScriptEngine SPI
├── fsdevtools-cli                    → Entry point (Main/Cli), Airline wiring, classpath scanning, export/import commands
├── fsdevtools-common                 → Shared impl: SimpleCommand base class, GlobalConfig, CliContextImpl, ConnectionBuilder
├── fsdevtools-sharedutils            → Connection utilities, string helpers
├── fsdevtools-serverrunner           → FirstSpirit server lifecycle management
├── fsdevtools-commands/              → Command modules (each independently buildable)
│   ├── feature                       → Content transport feature commands
│   ├── module                        → Module install/uninstall/configure
│   ├── project                       → Project import/export/delete
│   ├── schedule                      → Schedule task execution
│   ├── script                        → Script parse/run (BeanShell, Groovy, JS)
│   ├── server                        → Server start/stop
│   ├── service                       → Windows service management
│   ├── test                          → Connection/project testing
│   └── custom-command-example        → Template for custom command development
├── fsdevtools-scriptengines/
│   ├── Groovy                        → Groovy script engine plugin (shadow JAR)
│   └── Javascript                    → Nashorn script engine plugin (shadow JAR)
├── fsdevtools-docs                   → Vue.js documentation webapp
├── fsdevtools-docs-generator         → Generates command documentation JSON from annotations
└── buildSrc                          → Gradle extension functions (port allocation, version helpers)
```

## Architecture

### Command Execution Flow

```
Main.main() → Cli.execute(args)
  → ClassGraph scans classpath for @Command and @Group annotated classes
  → Airline CliBuilder registers all discovered commands
  → Airline parses args → instantiates Command
  → If command.needsContext() == true:
      Config → ConnectionBuilder → FirstSpirit Connection → CliContextImpl
      command.setContext(cliContext)
  → command.call() → Result<T>
  → JSON result file written if --resultFile specified (requires @JsonSerialize on Result class)
```

### Command Class Hierarchy

```
Command<Result> (interface, extends Callable)
  └── Config (interface — FS connection params: host, port, user, password, project, mode)
       └── GlobalConfig (abstract — @Option annotations for all FS connection options)
            └── SimpleCommand<Result> (abstract — base class for most commands)
                 └── ConcreteCommand (@Command annotated, implements call())
```

### Key Abstractions

- **`Command<R extends Result>`** (`fsdevtools-cli-api`): Core command interface extending `Callable<R>`
- **`Config`** (`fsdevtools-cli-api`): Declares FS connection parameters; if a command implements this, the CLI auto-creates a connection before `call()`
- **`GlobalConfig`** (`fsdevtools-common`): Implements `Config` with Airline `@Option` annotations for all connection params, reads defaults from environment variables (`fshost`, `fsport`, `fsuser`, `fspwd`, `fsmode`, `fsproject`, `fsservletzone`). Defaults: host=localhost, port=8000, mode=HTTP, user=Admin, password=Admin, servletzone=/
- **`SimpleCommand`** (`fsdevtools-common`): Extends `GlobalConfig`, base class for commands needing a FS connection
- **`CliContext` / `CliContextImpl`** (`fsdevtools-common`): Wraps FirstSpirit `Connection` + `SpecialistsBroker` with lifecycle management
- **`ConnectionBuilder`** (`fsdevtools-common`): Builds FirstSpirit connections from `Config`, handles HTTP/HTTPS/SOCKET modes and proxy settings
- **`Result`** (`fsdevtools-cli-api`): Command return value abstraction with error handling

### Adding a New Command

1. Create a class annotated with `@com.github.rvesse.airline.annotations.Command(name=..., groupNames=..., description=...)`
2. Extend `SimpleCommand<SimpleResult>` (or implement `Command<Result>` directly)
3. Place in any package under a command module — ClassGraph auto-discovers it at startup
4. For a new command group, create a class annotated with `@Group` providing a `defaultCommand`
5. See `fsdevtools-commands/custom-command-example` for a minimal example

### Script Engine Plugin System

External script engines are loaded from the `plugins/` directory using parent-first classloading. Implement `ScriptEngine` and `ScriptExecutable` from `fsdevtools-cli-api`, package as a shadow JAR.

## Testing

- **Framework**: JUnit 5 (Jupiter) + Mockito 4 + AssertJ
- **JVM test args** (fsdevtools-cli only): `--add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED`
- **Integration test ports**: Randomly allocated from range 30000-65535 via `guessFreePorts()` in buildSrc

## Key Conventions

- **Java 17** source and target compatibility, UTF-8 encoding
- **Dependency versions** managed via version catalogs in `settings.gradle.kts` (`libs` and `testlibs`)
- **Feature branch versioning**: Branches matching `*/ISSUE-KEY-*` automatically set version to `ISSUE-KEY-SNAPSHOT`
- **Release**: Only from `master` branch, managed by `net.researchgate.release` plugin
- **Logging**: SLF4J API + Log4J2 implementation; global `-e` CLI flag for verbose error output
- **FirstSpirit version**: Pinned in `build.gradle.kts` line 28 (`var firstSpiritVersion = "5.2.231105"`)
