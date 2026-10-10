# AGENTS.md

Guidance for coding agents working in this repository. `CLAUDE.md` imports this file, and this file is the only place to update.

## Project

CoApi is a Kotlin library that turns Spring HTTP Interface (`@HttpExchange`) interfaces annotated with `@CoApi` into injectable beans backed by `WebClient` (reactive) or `RestClient` (sync), with optional Spring Cloud LoadBalancer support.

- Group `me.ahoo.coapi`, artifacts `coapi-<module>`. Version in `gradle.properties`.
- Kotlin, JVM 17 toolchain (tests also run on 21 and 25 in CI). Spring Boot 4.x / Spring Framework 7.x. Versions in `gradle/libs.versions.toml`.

## Where things are documented

| Topic | Source of truth |
|-------|-----------------|
| User docs (EN) | `wiki/` (VitePress, published at https://coapi.ahoo.me). See `wiki/AGENTS.md` |
| User docs (zh) | `wiki/zh/`, a page-for-page mirror of `wiki/` |
| Internals, design rules, "where to change what" | `wiki/deep-dive/architecture.md` |
| Contribution process, CI gates, releases | `CONTRIBUTING.md` |
| Agent skill for CoApi *users* | `skills/coapi-developer/` |

Do not duplicate content from these places here. Link to them.

## Commands

```bash
./gradlew build                                   # compile, test, detekt, coverage gate
./gradlew :spring:test                            # one module
./gradlew :spring:test --tests "me.ahoo.coapi.spring.CoApiDefinitionTest"
./gradlew :spring:test -PtestJavaVersion=21       # tests on another JDK
./gradlew detekt                                  # autocorrects formatting locally, fails in CI
./gradlew codeCoverageReport codeCoverageVerification   # ≥95% line / ≥90% branch, aggregated
./gradlew :example-consumer-server:test :example-sync:test
```

## Modules

| Module | Role | Notes |
|--------|------|-------|
| `api` | `@CoApi`, `@LoadBalanced` | Annotations only, `spring-context` compile-only. Changing existing attributes is a breaking change. |
| `spring` | Registrars, `CoApiDefinition`, factory beans, client SPI, reactive auth filters | Optional feature variants: `reactiveSupport`, `lbSupport`, `jwtSupport` |
| `spring-boot-starter` | `CoApiAutoConfiguration`, `AutoCoApiRegistrar`, `CoApiProperties` | Feature variants `reactiveSupport`, `syncSupport`; kapt for configuration metadata |
| `bom` / `dependencies` | Published BOM / internal version platform | `dependencies` affects every module |
| `example/*` | Runnable examples, also integration-tested in CI | Do not remove: referenced by docs and CI |
| `code-coverage-report` | Aggregated JaCoCo report and coverage gate | |

## Invariants (keep them)

- URL and load-balancing precedence live only in `CoApiDefinition.normalize()` / `withOverrides()`. Everything else consumes `effectiveDefinition()`.
- `CoApiRegistrar` never replaces existing bean definitions, so applications can override any bean.
- Spring Cloud types are referenced only from the `internal` `LoadBalanced*BuilderCustomizer` classes, which are loaded only for load-balanced clients.
- `WebClientFactoryBean` / `RestClientFactoryBean` are final. Extension goes through customizer beans and the optional `ClientProperties` / `ReactiveClientProperties` / `SyncClientProperties` roles.
- Misconfiguration fails at startup with a message naming the client and the fix.

## Conventions

- Tests: JUnit 5, MockK, assertions with `me.ahoo.test.asserts.assert` (fluent-assert), not AssertJ's `assertThat`. Exception: `ApplicationContextRunner` context assertions use `AssertionsForInterfaceTypes.assertThat(context)` as existing tests do. Tests must not call external services.
- Compiler: `-Xjsr305=strict`, `-jvm-default=enable`, `-parameters`, warnings are errors in library modules.
- Detekt config: `config/detekt/detekt.yml` (max line length 300). Every source file starts with the Apache 2.0 header (`config/detekt/license.template`).
- PR titles follow Conventional Commits (PRs are squash-merged).

## Documentation rules

- A user-visible behavior or configuration change updates the affected `wiki/` page **and** its `wiki/zh/` mirror in the same PR. A breaking change also extends `wiki/getting-started/migration-v3.md` (or the next major's guide).
- Update `README.md` / `README.zh-CN.md` only for the pitch, compatibility table, installation and the minimal example. Details belong in the wiki.
- If the change affects facts in `skills/coapi-developer/`, update them there too.
