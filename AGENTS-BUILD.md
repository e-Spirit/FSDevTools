# Build Commands

```bash
# Full build (all subprojects)
./gradlew build

# Run all tests
./gradlew test

# Run tests in a single subproject
./gradlew :fsdevtools-commands:feature:test

# Run a single test class
./gradlew :fsdevtools-commands:project:test --tests "com.espirit.moddev.cli.commands.project.ProjectExportCommandTest"

# Build the uber JAR only
./gradlew shadowJar
# → build/shadowJAR/fsdevtools-cli-<version>.jar

# Build the distribution archives
./gradlew assembleTarGz
./gradlew assembleZip

# Full assemble (both archives + docs)
./gradlew assemble

# Generate documentation JSON (consumed by fsdevtools-docs)
./gradlew createDocumentationJson

# Build Vue.js docs only
./gradlew :fsdevtools-docs:buildVueApp

# Publish to Artifactory
./gradlew publish
```

**Credentials required:** Builds that resolve FirstSpirit dependencies need `artifactory_username` and `artifactory_password` in `~/.gradle/gradle.properties` (or as `-P` flags). Without them, the build falls back to `mavenLocal()` + `mavenCentral()`, which will fail to resolve `fs-isolated-runtime`.

**FirstSpirit EAP builds:** Pass `-PuseLatestFirstSpiritBuild` to resolve `fs-isolated-runtime:EAP-SNAPSHOT` instead of the pinned version.

---

## Versioning

Follows **semantic versioning** (MAJOR.MINOR.PATCH). On feature branches the version is derived from the ticket ID in the branch name at build time:

```kotlin
// root build.gradle.kts
Regex("(?:.*/)?[^A-Z]*([A-Z]+-[0-9]+).*").matchEntire(branchName)?.let {
    project.version = "${it.groupValues[1]}-SNAPSHOT"
}
```

A branch named `feature/CORE-1234-my-feature` produces version `CORE-1234-SNAPSHOT`. On `master`, `gradle.properties` provides the release version. Releases are cut from `master` only via the `net.researchgate.release` Gradle plugin.

---

## Dependency Catalog (`settings.gradle.kts`)

| Alias | Artifact | Version |
|---|---|---|
| `libs.airline` | `com.github.rvesse:airline` | 2.8.5 |
| `libs.jackson.databind` | `com.fasterxml.jackson.core:jackson-databind` | 2.18.3 |
| `libs.log4j.core` | `org.apache.logging.log4j:log4j-core` | 2.24.3 |
| `libs.slf4j.api` | `org.slf4j:slf4j-api` | 2.0.17 |
| `libs.guava` | `com.google.guava:guava` | 33.4.7-jre |
| `libs.commons.compress` | `org.apache.commons:commons-compress` | 1.27.1 |
| `libs.classgraph` | `io.github.classgraph:classgraph` | 4.8.179 |

Test: `testlibs.assertj`, `testlibs.mockito`, `testlibs.archunit`

---

## CI/CD (`ci.yaml`)

- **EAP build** — runs on a schedule against `firstSpiritVersion=EAP-SNAPSHOT` (via `-PuseLatestFirstSpiritBuild`)
- **Release flow** — merging to `master` triggers `net.researchgate.release` → version bump commit → GitHub Release with `fs-cli-<version>.tar.gz`, `fs-cli-<version>.zip`, and one JAR per `fsdevtools-scriptengines` submodule → Artifactory publish
- **GitHub Release assets:** produced by `assembleTarGz`, `assembleZip`, and the per-engine shadow JAR tasks registered in the root build
