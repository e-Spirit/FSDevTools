# fsdevtools-docs-generator — Documentation JSON Generator

Standalone Java application that introspects all CLI commands at runtime and produces `data.json` for the Vue.js docs UI. Invoked from the root via `./gradlew createDocumentationJson`.

## Reference Files

| File | When to read |
|---|---|
| `AGENTS-GENERATOR.md` | How the generator works, the Gradle trigger, and what's needed when adding new commands |
| `AGENTS-BUILD.md` | Entry point, dependencies, and build setup |
