# Custom Command Template

## Key Classes

| Class | Role |
|---|---|
| `ExampleCustomGroup` | `@Group(name = "example")` — minimal group declaration |
| `ExampleCustomCommand` | `@Command` implementation — shows the minimum required structure |

## How to Use This as a Template

To create your own command extension:

1. Create a new project (or module)
2. Add `fs-cli.jar` (the uber JAR) as a `compileOnly` dependency
3. Add `fsdevtools-cli-api` as `compileOnly`
4. Implement:
   - A class annotated with `@Group` (optional — only needed for grouped commands)
   - One or more classes implementing `Command<R>` annotated with `@com.github.rvesse.airline.annotations.Command`
   - Optionally implement `Config` if the command needs a FirstSpirit connection
5. Build a **fat JAR** (uber JAR) including your classes and all runtime dependencies
6. Drop the fat JAR into the `plugins/` directory of the fs-cli installation

The CLI discovers commands via ClassGraph scanning at startup — no registration needed.

## Classloader Constraints

Plugin JARs share the parent-first classloader. You **cannot** override classes from:
- The CLI itself
- `fs-isolated-runtime.jar`
- Jackson, SLF4J, Guava, Commons, Log4J, Airline

Bundle only your own code and unique dependencies that are not already in the CLI.
