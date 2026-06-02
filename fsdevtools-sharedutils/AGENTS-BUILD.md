# Build, Dependencies & Guidelines

## Dependencies

- `org.apache.commons:commons-compress` (1.27.1)

## Build

```bash
./gradlew :fsdevtools-sharedutils:build
./gradlew :fsdevtools-sharedutils:test
```

## Guidelines

Keep this module **extremely lean**. Only add code here when it is needed by `fsdevtools-cli-api` itself and cannot be placed in `fsdevtools-common` (which depends on `fsdevtools-cli-api`, so it cannot be used by `fsdevtools-cli-api` without a cycle).
