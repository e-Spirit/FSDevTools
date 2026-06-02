# Shared Utilities — API & Dependencies

## Key Packages

Look in `src/main/java/com/espirit/moddev/` for:

- **File utilities** — reading/writing files, path manipulation
- **Compression utilities** — archive creation and extraction (backed by `commons-compress`)
- **JSON utilities** (`JacksonUtil`) — standardized `ObjectMapper` creation (used by `Cli` for result files)

## Dependencies (exposed as `api`)

| Library | Purpose |
|---|---|
| `com.google.guava:guava` (33.4.7-jre) | Collections, IO, utilities |
| `org.apache.commons:commons-lang3` (3.17.0) | String/array/reflection utilities |
| `org.apache.commons:commons-compress` (1.27.1) | Archive (tar, zip, …) support |
| `com.fasterxml.jackson.core:jackson-databind` (2.18.3) | JSON serialization |

All four are transitive `api` dependencies — consuming modules inherit them automatically.

Also depends on `fsdevtools-cli-api` (as `implementation`).

## Usage Pattern

Command modules import this module to get Guava, Commons Lang3, Commons Compress, and Jackson without declaring them individually:

```kotlin
// in a command module's build.gradle.kts
dependencies {
    implementation(project(":fsdevtools-common"))
}
```
