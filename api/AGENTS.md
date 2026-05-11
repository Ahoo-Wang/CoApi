# AGENTS.md — api Module

## Build & Run

```bash
./gradlew :api:build
./gradlew :api:test  # (no tests in this module currently)
```

## Module Role

Core annotations module. Contains only `@CoApi` and `@LoadBalanced` annotations. Has minimal dependencies (`spring-context` as `compileOnly`).

## Key Files

- `src/main/kotlin/me/ahoo/coapi/api/CoApi.kt` — `@CoApi` annotation with `baseUrl`, `serviceId`, `name` parameters. Also meta-annotated with `@Component`.
- `src/main/kotlin/me/ahoo/coapi/api/LoadBalanced.kt` — Marker annotation for load-balanced clients.

## Boundaries

- ✅ Add new annotation parameters (binary-compatible additions only)
- ⚠️ Changing existing annotation parameters is a breaking change
- 🚫 Do not add implementation logic here — this module is annotations only
