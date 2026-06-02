# Command Authoring

## How command discovery works

`Cli` uses ClassGraph to scan the entire classpath at **class-load time** (once, statically) for:
- All classes implementing `Command` annotated with `@com.github.rvesse.airline.annotations.Command`
- All classes annotated with `@com.github.rvesse.airline.annotations.Group`

No manual registration is needed. Making the module reachable from `fsdevtools-cli`'s classpath is sufficient.

## Checklist to add a new command

1. **Group class** — annotate with `@Group(name = "mygroup")` (one per group; skip if adding to an existing group)
2. **Command class** — implement `Command<MyResult>`, annotate with `@com.github.rvesse.airline.annotations.Command(name = "mycommand", groupNames = {"mygroup"})`
3. **Config** — implement `Config` if the command needs a FirstSpirit connection; the CLI then establishes the connection before `call()`. Omit for connection-free commands.
4. **Result** — implement `Result<T>`; annotate the result payload with `@JsonSerialize` to enable the JSON result file that `Cli.executeCommand()` writes automatically.
5. **Module wiring** — add the module as a dependency of `fsdevtools-commands` (or directly of `fsdevtools-cli`); register in `settings.gradle.kts`.

## Command execution flow

```
Main.main(args)
  └─ Cli.main(args)
       └─ new Cli().execute(args)
            ├─ ClassGraph scan → command & group classes (class-load time, once)
            ├─ Airline CliBuilder.build() → parser
            ├─ parseCommandLine(args)
            └─ executeCommand(command)
                 ├─ if command instanceof Config → new CliContextImpl(config) → config.setContext(ctx)
                 ├─ command.call()                        // command logic
                 ├─ result.log()
                 ├─ writeCommandResultToResultFile()      // only if result payload is @JsonSerialize
                 └─ context.close()
```

---

## Implementation Ownership

| Change type | Touch these modules |
|---|---|
| New command in an existing group | `fsdevtools-commands:<group>/` only |
| New command group | `fsdevtools-commands/<newgroup>/` + `settings.gradle.kts` + dependency in `fsdevtools-commands` |
| New API interface / contract | `fsdevtools-cli-api/` |
| New shared utility (depends on `fsdevtools-cli-api`) | `fsdevtools-common/` |
| New shared utility (needed by `fsdevtools-cli-api` itself) | `fsdevtools-sharedutils/` |
| New script engine plugin | `fsdevtools-scriptengines/<EngineName>/` + `settings.gradle.kts` |
| Docs update (content / structure) | `fsdevtools-docs/` + `fsdevtools-docs-generator/` |
