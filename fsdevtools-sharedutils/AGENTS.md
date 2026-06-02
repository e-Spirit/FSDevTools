# fsdevtools-sharedutils — Low-Level Shared Utilities

Lowest-level module: contains only utilities needed by `fsdevtools-cli-api` itself. Exposed transitively to all consumers via `cli-api`'s `api` dependency. Keep it extremely lean — anything that can go in `fsdevtools-common` should go there.

## Reference Files

| File | When to read |
|---|---|
| `AGENTS-BUILD.md` | Dependencies, build commands, and guidelines for what belongs in this module |
