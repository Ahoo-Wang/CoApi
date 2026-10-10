---
name: coapi-developer
description: >
  Help developers use and maintain CoApi, a Spring HTTP Interface client auto-configuration library for typed
  @HttpExchange proxies backed by reactive WebClient or synchronous RestClient. Use this skill when the user mentions
  CoApi, @CoApi, @EnableCoApi, @LoadBalanced, @HttpExchange, Spring HTTP Interface clients, Spring Boot
  auto-configuration for HTTP clients, reactive vs sync client mode, Spring Cloud LoadBalancer integration, or repository
  classes such as CoApiDefinition, CoApiFactoryBean, AutoCoApiRegistrar, EnableCoApiRegistrar, ClientMode,
  WebClientFactoryBean, RestClientFactoryBean, ReactiveHttpExchangeAdapterFactory, SyncHttpExchangeAdapterFactory,
  or properties such as coapi.mode, coapi.base-packages, and coapi.clients.
---

# CoApi Developer Skill

Use this skill to help with **CoApi** (`me.ahoo.coapi`), which turns Spring `@HttpExchange`
interfaces into Spring beans with minimal setup. CoApi is Kotlin-first, supports Spring Boot 4.x /
Spring Framework 7.x in the current codebase, and can create either reactive `WebClient` or
synchronous `RestClient` proxies.

## How To Work

1. Identify the request type, then load only the needed reference file:
   - New client interface, application config, load balancing, or examples: read [references/usage.md](references/usage.md).
   - Architecture, module boundaries, annotations, mode selection, or implementation edits: read [references/concepts.md](references/concepts.md).
   - Tests, assertions, `ApplicationContextRunner`, integration tests, or MockK: read [references/testing.md](references/testing.md).
   - Startup failures, missing beans, client mode surprises, base URL issues, or load-balancer problems: read [references/troubleshooting.md](references/troubleshooting.md).
2. Prefer concise, working Kotlin snippets unless the user asks for Java.
3. Recommend the simplest CoApi-supported approach first, then name the tradeoff when another approach is valid.
4. If the request is outside CoApi's purpose, say so directly and route the user to the Spring tool that owns it.

## Essential Facts

- Dependency: `implementation("me.ahoo.coapi:coapi-spring-boot-starter")`, plus the builder for the client mode: `spring-boot-starter-webclient` (reactive) or `spring-boot-starter-restclient` (sync). The starter does not bring either.
- `AUTO` mode picks `REACTIVE` whenever Spring WebFlux is on the classpath (a classpath check, not the web application type); set `coapi.mode` explicitly when that is wrong.
- Docs: https://coapi.ahoo.me (troubleshooting: `/getting-started/troubleshooting`).
- Main annotations:
  - `@CoApi(baseUrl = "...", serviceId = "...", name = "...")` marks an interface as a CoApi client.
  - `@LoadBalanced` explicitly enables load-balanced client wiring.
  - `@EnableCoApi(clients = [...])` explicitly registers client interfaces.
- Client mode: `coapi.mode` supports `AUTO`, `REACTIVE`, and `SYNC`; an invalid value fails startup listing the valid options (v2.2.0).
- Base URL resolution has two layers: `@CoApi(baseUrl)` wins over `serviceId`; `serviceId`/`lb://` targets are normalized to `http://...` with `loadBalanced=true`; `coapi.clients.<name>.base-url` can override the parsed definition at HTTP client factory time.
- Placeholders in `baseUrl`/`serviceId` must resolve in the active environment (v2.2.0): an unresolvable `${...}` fails startup with `Could not resolve placeholder ...`. Use `${name:default}` for an inline fallback.
- Client names must be unique (v2.2.0): the name defaults to the interface simple name; two clients resolving to the same name fail startup with the conflicting types listed. The name is also the key for `coapi.clients.<name>.*`.
- `coapi.clients.<name>.load-balanced` is a real three-state override: `true` enables, `false` disables; unset follows a configured `base-url` (load balanced only for `lb://`, v2.3.0), else the annotation. `lb://` matches case-insensitively (v2.3.0).
- Load balancing requires `spring-cloud-starter-loadbalancer`; without it a load-balanced client fails with an actionable message (v3.0.0). CoApi resolves the sync interceptor by the `BlockingLoadBalancerInterceptor` interface, which also works when Spring Cloud retry is enabled.
- Extension SPI (v3.0.0): customize clients with `WebClientBuilderCustomizer` / `RestClientBuilderCustomizer` beans, which receive the *effective* definition (overrides applied); the factory beans are final. Per-client config roles are `ClientProperties` (base URL / load balancing), `ReactiveClientProperties` (filters) and `SyncClientProperties` (interceptors), all optional. Migration from 2.x: `wiki/getting-started/migration-v3.md`.
- `CoApiDefinition` beans must come from a static `@Bean` method (Kotlin: `@JvmStatic` in a `companion object`); a non-static one fails startup (v3.0.0, a warning in v2.3.0). CoApi resolves `${...}` and `lb://` in their `baseUrl`.
- Prefer `me.ahoo.test.asserts.assert` for value assertions. For `ApplicationContextRunner` bean assertions, mirror existing tests with `AssertionsForInterfaceTypes.assertThat(context)`.

## CoApi Boundaries

CoApi is for declarative request-response HTTP clients built from typed Spring interfaces. It does not
own WebSocket or SSE clients, resilience policies, arbitrary one-off HTTP calls, or non-Spring runtime
wiring. For those concerns, use Spring `WebClient`, `RestClient`, `WebSocketClient`, filters,
interceptors, or a resilience library such as Resilience4j.

## Minimal Client Pattern

```kotlin
@CoApi(baseUrl = "\${github.url}")
interface GitHubApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssue(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}
```

```yaml
github:
  url: https://api.github.com
coapi:
  mode: AUTO
```
