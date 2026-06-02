# Documentation Generator — How It Works

## Entry Point

```
com.espirit.moddev.cli.documentation.commands.CommandDocumentationGenerator
```

## How It Works

1. Uses **ClassGraph** to scan for all `@Command` and `@Group` annotated classes on the classpath
2. Uses **Airline** (`airline-help-markdown`) to extract command metadata: name, description, options, option descriptions, examples
3. Serializes everything to JSON via **Jackson**
4. Writes the output to the path given by `--file` argument

## Trigger from Root

The generator is not run as a standalone Gradle task in the subproject — it's invoked from the root:

```kotlin
// root build.gradle.kts
val createDocumentationJson by tasks.registering(JavaExec::class) {
    mainClass = "com.espirit.moddev.cli.documentation.commands.CommandDocumentationGenerator"
    args = listOf("--file", "${project(":fsdevtools-docs").projectDir}/build/assets/data.json")
    classpath = project(":fsdevtools-docs-generator").sourceSets.main.get().runtimeClasspath
}
```

```bash
./gradlew createDocumentationJson
# → writes to fsdevtools-docs/build/assets/data.json
```

## Extension

When you add a new command or group, it is automatically included in the generated documentation as long as it is:
1. On the classpath of `fsdevtools-docs-generator` (i.e. reachable transitively through `fsdevtools-cli`)
2. Annotated with `@com.github.rvesse.airline.annotations.Command` or `@Group`
