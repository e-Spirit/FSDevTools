# Technology Stack & Notes

## Technology Stack

- **Vue.js** (Node.js 12.18.1, managed by the `com.github.node-gradle.node` Gradle plugin)
- npm for package management
- Bundled via `npm run build` → `dist/` directory
- The built `dist/` output is included in the distribution archives under `fs-cli/docs/`

## Node.js Version

Node.js 12.18.1 is **downloaded automatically** into `.gradle/nodejs/` — no local Node installation required. The `download = true` setting in `build.gradle.kts` handles this.

## Notes

- Publishing is disabled for this module
- The `jar` task is enabled and bundles the built Vue app for embedding (includes `fsui` images from `node_modules/fsui/dist/static`)
