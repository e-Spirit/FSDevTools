# Build

```bash
# Build all script engines
./gradlew :fsdevtools-scriptengines:build

# Build individual engine fat JARs
./gradlew :fsdevtools-scriptengines:Groovy:shadowJar
./gradlew :fsdevtools-scriptengines:Javascript:shadowJar
```

Output: `build/shadowJAR/<EngineName>-<version>.jar`
