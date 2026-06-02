# CLI Application — Overview

## Key Classes

| Class | Role |
|---|---|
| `Main` | JVM entry point — sets up SLF4J provider, delegates to `Cli.main()` |
| `Cli` | Core CLI orchestrator — command discovery, parsing, context creation, execution |
| `CliContext` / `CliContextImpl` | Wraps the FirstSpirit `ProjectScriptContext` |
| `CliBuilderHelper` | Registers all discovered command groups with Airline |
| `CliConstants` | Enum of configuration keys (`FS_CLI`, `fshost`, `fsport`, …) |
| `CommandUtils` | Classpath scanner for `Command` implementations |
| `GroupUtils` | Classpath scanner for command group classes |
| `SystemExitHandler` | Translates termination outcomes to `System.exit()` codes |
| `FsLoggingBridge` | Bridges FirstSpirit's internal `Logging` to SLF4J |

## How Command Discovery Works

`Cli` uses ClassGraph (via `CommandUtils` and `GroupUtils`) to scan the classpath at **class-load time** for:
- All classes implementing `Command`
- All classes annotated with `@Group`

These are registered dynamically with the Airline `CliBuilder`. No manual registration is needed when adding a new command module — add the dependency in `build.gradle.kts` and annotate the class.

## Execution Flow

```
Main.main(args)
  └─ Cli.main(args)
       └─ new Cli().execute(args)
            ├─ setLoggingSystemProperties()     // Log4J dir, FS logging bridge
            ├─ logVersionsAndGitHash()           // reads CliBuild.properties, CliGit.properties
            ├─ getDefaultCliBuilder()            // ClassGraph scan → Airline builder
            ├─ parseCommandLine(args, builder)   // Airline parsing
            └─ executeCommand(command)
                 ├─ getCliContextOrNull()        // creates FS connection if command implements Config
                 ├─ command.call()               // actual command logic
                 ├─ result.log()
                 ├─ writeCommandResultToResultFile()  // JSON result file (if @JsonSerialize)
                 └─ context.close()
```

## Result File

Commands that implement `Config` and return a `@JsonSerialize`-annotated result object will have their result written to a JSON file. The file path is provided via `Config.getResultFile()`. The file is wrapped in `WrappedCommandResult` or `WrappedExceptionResult`.
