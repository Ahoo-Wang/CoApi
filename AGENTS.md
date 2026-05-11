# AGENTS.md — CoApi

> **Note**: This file provides project-specific context for coding agents. See [CLAUDE.md](./CLAUDE.md) for detailed guidance.

## Build & Run Commands

```bash
./gradlew build                          # Build all modules
./gradlew :spring:test                   # Test spring module
./gradlew :spring-boot-starter:test      # Test boot-starter module
./gradlew detekt                         # Static analysis
./gradlew publishToMavenLocal            # Publish locally
./gradlew :code-coverage-report:jacocoTestReport  # Coverage report
```

Run a single test: `./gradlew :spring:test --tests "me.ahoo.coapi.spring.CoApiDefinitionTest"`

## Testing

- **Framework**: JUnit 5, MockK, `fluent-assert` (`me.ahoo.test.asserts.assert`)
- **Test retry**: Auto-retry up to 2 times in CI
- **Log config**: `config/logback.xml`

## Tech Stack

- Kotlin (JVM 17), Spring Boot 4.x / Spring Framework 7.x
- Gradle Kotlin DSL, Detekt + ktlint
- Published to Maven Central as `me.ahoo.coapi:*`

## Module Structure

| Module | Role |
|--------|------|
| `api` | `@CoApi`, `@LoadBalanced` annotations |
| `spring` | Registrar, FactoryBean, client SPI (reactive + sync) |
| `spring-boot-starter` | Boot auto-configuration, `CoApiProperties` |
| `bom` | Bill of Materials |
| `dependencies` | Version management platform |
| `example/*` | Provider-consumer examples |

## Conventions

- Kotlin code style: official (`kotlin.code.style=official`)
- Compiler flags: `-Xjsr305=strict`, `-Xjvm-default=all-compatibility`, `-parameters`
- Max line length: 300 (detekt)
- Assertions in tests: use `me.ahoo.test.asserts.assert`, NOT AssertJ's `assertThat`

## Boundaries

- ✅ Modify `api`, `spring`, `spring-boot-starter` source code
- ✅ Add tests to existing test directories
- ✅ Update `example/` applications
- ⚠️ Changing public API in `api/` requires major version bump
- ⚠️ Modifying `dependencies/` affects all modules
- 🚫 Do not modify `build.gradle.kts` root build logic without understanding the full impact
