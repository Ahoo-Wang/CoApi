# AGENTS.md — spring-boot-starter Module

## Build & Run

```bash
./gradlew :spring-boot-starter:build
./gradlew :spring-boot-starter:test
```

## Module Role

Spring Boot auto-configuration. Depends on `spring` module. Provides `CoApiAutoConfiguration`, `CoApiProperties`, and `AutoCoApiRegistrar` for classpath-scanning discovery.

## Key Files

- `src/main/kotlin/.../CoApiAutoConfiguration.kt` — `@AutoConfiguration` entry point
- `src/main/kotlin/.../AutoCoApiRegistrar.kt` — Classpath scanner + `@CoApi` discovery
- `src/main/kotlin/.../CoApiProperties.kt` — `@ConfigurationProperties(prefix = "coapi")`
- `src/main/kotlin/.../ConditionalOnCoApiEnabled.kt` — `@ConditionalOnProperty("coapi.enabled")`

## Feature Variants (build.gradle.kts)

- `reactiveSupport`: spring-boot-webclient
- `syncSupport`: spring-boot-restclient
- Uses `kapt` for configuration-processor and autoconfigure-processor

## Testing

- `ApplicationContextRunner` for context boot tests
- Tests reactive and sync modes
- Tests `basePackages` scanning (single, multiple, YAML list)
- Assertions: `me.ahoo.test.asserts.assert`

## Boundaries

- ✅ Add new configuration properties to `CoApiProperties`
- ⚠️ Changes to `AutoCoApiRegistrar` affect all Spring Boot users
- 🚫 Do not modify `@ConditionalOnCoApiEnabled` without understanding the impact
