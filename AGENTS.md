# AGENTS.md

Instructions for coding agents in this repository. `CLAUDE.md` only imports this file: edit here.

CoApi is a Kotlin library: annotate a Spring HTTP Interface (`@HttpExchange`) with `@CoApi` and it becomes an injectable bean backed by `WebClient` (reactive) or `RestClient` (sync), optionally load-balanced through Spring Cloud. Kotlin on a JVM 17 toolchain, Spring Boot 4 / Spring Framework 7. Versions: `gradle.properties` (project), `gradle/libs.versions.toml` (dependencies).

## Read before you change

| You are about to… | Read first |
|---|---|
| Change anything in `spring/` or `spring-boot-starter/` | `wiki/deep-dive/architecture.md`: registration flow, per-client bean graph, design rules, and a "where to change what" table mapping each concern to its files and tests |
| Edit anything under `wiki/` | `wiki/AGENTS.md`: layout, sidebar, writing rules |
| Open a PR or cut a release | `CONTRIBUTING.md` |
| Answer how to *use* CoApi | `skills/coapi-developer/` and the user docs in `wiki/` |

## Commands

```bash
./gradlew build                                   # everything CI gates: compile, tests, detekt, coverage
./gradlew :spring:test --tests "me.ahoo.coapi.spring.CoApiDefinitionTest"   # one class (add .method for one test)
./gradlew :spring-boot-starter:test               # one module
./gradlew :spring:test -PtestJavaVersion=21       # run tests on another JDK (CI: 17, 21, 25)
./gradlew detekt                                  # lint; autocorrects formatting locally only
./gradlew codeCoverageReport codeCoverageVerification   # aggregated gate: ≥95% line, ≥90% branch
./gradlew :example-provider-server:check :example-consumer-server:check :example-sync:check
cd wiki && pnpm install && pnpm build             # docs; fails on dead internal links
```

## Design rules

They protect users, so do not break them. `architecture.md` explains why.

1. **One owner for precedence.** Base URL and load-balancing precedence are computed only in `CoApiDefinition.normalize()` / `withOverrides()`. Everything else consumes `effectiveDefinition()` and never re-derives it.
2. **Users can override every bean.** `CoApiRegistrar` registers a definition only if it is absent.
3. **Optional dependencies stay optional.** `spring` has feature variants (`reactiveSupport`, `lbSupport`, `jwtSupport`). Spring Cloud types appear only in the `internal` `LoadBalanced*BuilderCustomizer` classes, loaded only for load-balanced clients after `requireLoadBalancerSupport` checks the classpath.
4. **Extend by composition.** `WebClientFactoryBean` / `RestClientFactoryBean` are final. New behavior goes through customizer beans or the `ClientProperties` / `ReactiveClientProperties` / `SyncClientProperties` roles.
5. **Fail fast.** Misconfiguration throws at startup with a message naming the client and the fix, and `wiki/getting-started/troubleshooting.md` lists that message.
6. **Public API is a contract.** `api/` annotations, `coapi.*` properties and the SPI in `spring/` follow SemVer. A breaking change needs a `!` in the PR title and a migration guide entry.

## Non-obvious facts

- **Kotlin warnings are errors** in library modules (`api`, `spring`, `spring-boot-starter`), not in examples.
- **detekt behaves differently in CI.** Locally it rewrites formatting in place. In CI (`CI` env set) the same issues fail the build. Run `./gradlew detekt` and keep its changes.
- **License header.** Every source file starts with `config/detekt/license.template`, and detekt enforces it.
- **Flaky tests fail.** In CI, test-retry reruns failed tests, but `failOnPassedAfterRetry = true` still fails the build.
- **Examples are test fixtures.** `spring` and `spring-boot-starter` tests depend on `:example-provider-api` and `:example-consumer-client`, so editing those modules can break library tests. CI and the docs also reference every module under `example/`.
- **Coverage is aggregated** across library modules in `code-coverage-report`, and `check` enforces it. New code without tests can fail `build` even when every test passes.
- **`mavenLocal()` is resolved first.** A stale `~/.m2` artifact can shadow the real dependency.
- Configuration metadata for `CoApiProperties` comes from kapt (`spring-boot-configuration-processor`), so don't hand-write it. Auto-configuration is registered in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

## Tests

- JUnit 5 + MockK. Assert with fluent-assert (`me.ahoo.test.asserts.assert`, `assertThrownBy`), not AssertJ `assertThat`. One exception: `ApplicationContextRunner` checks use `AssertionsForInterfaceTypes.assertThat(context)`, as in `CoApiAutoConfigurationTest`.
- No network calls to external services. Use a local stand-in, as `ConsumerServerTest` does.
- For a bug, write a failing test first.

## A change is done when

- [ ] `./gradlew build` passes (plus the affected `example-*:check` if you touched examples or wiring).
- [ ] User-visible behavior or configuration is documented in the affected `wiki/` page **and** its `wiki/zh/` mirror.
- [ ] Adding, renaming or removing a wiki page also updates `wiki/.vitepress/config.mts` (`sidebar()`) and `llms.txt`.
- [ ] A breaking change extends `wiki/getting-started/migration-v3.md` (or the current major's guide).
- [ ] Facts that changed are updated in `skills/coapi-developer/` too.
- [ ] `README.md` / `README.zh-CN.md` change only for the pitch, compatibility table, installation or the minimal example.
- [ ] The PR title follows Conventional Commits (it becomes the squash commit), e.g. `fix(spring): …`, `feat(starter)!: …`.

## Maintaining this file

Keep it short. Add a line only if it changes what an agent does and can't be learned quickly from the code. Link to the source of truth (`architecture.md`, `CONTRIBUTING.md`, `wiki/AGENTS.md`) instead of copying it, and check every command before writing it down.
