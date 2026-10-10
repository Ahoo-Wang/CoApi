---
title: What is CoApi?
description: CoApi turns Spring HTTP Interface (@HttpExchange) declarations into injectable Spring beans, backed by WebClient or RestClient, with optional client-side load balancing.
---

# What is CoApi?

Spring's [HTTP Interface](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface) lets you declare an HTTP API as a Java/Kotlin interface with `@HttpExchange` methods. To call it, something still has to build an HTTP client, wrap it in an adapter, create a proxy with `HttpServiceProxyFactory` and register that proxy as a bean, once per interface.

CoApi does that wiring for you. Annotate the interface with `@CoApi`, and you can inject it:

```kotlin
@CoApi(baseUrl = "\${github.url}")
interface GitHubApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssues(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}

@RestController
class IssueController(private val gitHubApiClient: GitHubApiClient)
```

## What you get

| Capability | How |
|------------|-----|
| One injectable proxy per interface, no factory code | `@CoApi` + Spring Boot auto-configuration, or `@EnableCoApi` |
| Reactive or blocking | `WebClient` or `RestClient`, chosen by `coapi.mode` or inferred from the classpath |
| Client-side load balancing | `serviceId`, `lb://` or `@LoadBalanced`, via Spring Cloud LoadBalancer |
| Per-client settings without code | `coapi.clients.<name>.*` overrides base URL, load balancing, filters and interceptors |
| Global hooks | `WebClientBuilderCustomizer` / `RestClientBuilderCustomizer` beans |
| Token authentication | `BearerTokenFilter` with a refreshing token cache (reactive) |

## When to use it

CoApi fits when you call several HTTP APIs through typed interfaces and want each one to be a bean with minimal setup, in either programming model. In particular:

- **Reactive applications.** Spring Cloud OpenFeign has no reactive support. CoApi treats `WebClient` as a first-class option.
- **Shared contracts.** A provider can implement an `@HttpExchange` interface, and consumers extend it with `@CoApi` (see [Examples](../deep-dive/examples.md)).
- **Service discovery.** Point a client at a `serviceId` instead of a host.

CoApi is not the right tool for WebSocket or SSE clients, for resilience policies (retry, circuit breaking: use filters/interceptors with a library such as Resilience4j), or for one-off calls where a typed interface adds nothing. Use `WebClient` or `RestClient` directly for those.

## How it works

For every client interface, CoApi registers two beans:

1. `<name>.HttpClient`: a `WebClient` or `RestClient` built with the client's base URL, its filters or interceptors, and load balancing when enabled.
2. `<name>.CoApi`: the interface proxy created by `HttpServiceProxyFactory` on top of that client. This is the bean you inject.

`<name>` is `@CoApi(name)`, or the interface's simple name by default. [Architecture](../deep-dive/architecture.md) describes the full registration flow.

## Version compatibility

| CoApi | Spring Boot | Spring Framework | JDK |
|-------|-------------|------------------|-----|
| 3.x | 4.x | 7.x | 17+ |
| 2.x | 4.x | 7.x | 17+ |
| 1.x | 3.2.x | 6.1.x | 17+ |

The current line is tested on JDK 17, 21 and 25. Upgrading from 2.x? Read [Migrating to 3.0](./migration-v3.md).

## Next steps

- [Installation](./installation.md): add the dependencies.
- [Quick Start](./quick-start.md): define and call your first client.
