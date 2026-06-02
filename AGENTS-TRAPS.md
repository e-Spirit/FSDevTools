# Agent Traps

Known pitfalls that have caused incorrect edits or wasted investigation time — read before making changes.

- **Hardcoded dev path in `fsdevtools-cli/build.gradle.kts`:** There is a `runtimeOnly(files("/home/windmueller/..."))` line pointing to a developer's local `fs-isolated-runtime.jar`. This is intentional for local development and has no effect in CI (where the JAR is on the classpath via `testRuntimeJar`). Do not remove it, but do not treat it as a reliable dependency declaration.
- **`disablePublishing()` is not a built-in Gradle method** — it is defined in `buildSrc`. If you see `disablePublishing()` in a subproject's `build.gradle.kts` and cannot find the definition, look in `buildSrc/`.
- **`fsdevtools-docs` requires Node.js** — downloaded automatically into `.gradle/nodejs/` (version 12.18.1); no local Node installation is required. The `buildVueApp` task depends on `createDocumentationJson`, which requires the full CLI classpath to be compiled first. Run `./gradlew createDocumentationJson` before attempting to serve the Vue app locally.
- **Command discovery is ClassGraph-based, not Spring or ServiceLoader** — there is no registry to update when adding a command. If a new command is not being picked up, verify that its module is on the `fsdevtools-cli` classpath and that the class carries both `@Group`/`@Command` and implements `Command`.
- **Test JVM flags** — `fsdevtools-cli` tests require `--add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED` and `--add-opens=java.base/java.util=ALL-UNNAMED`. Configured in `fsdevtools-cli/build.gradle.kts`. If tests in that module throw `InaccessibleObjectException`, check those flags first.
