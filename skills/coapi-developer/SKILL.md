---
name: coapi-developer
description: >
  Help developers use the CoApi library — a Spring Framework HTTP client auto-configuration library that supports
  both reactive (WebClient) and synchronous (RestClient) programming models. Use this skill whenever the user mentions
  CoApi, @CoApi, @HttpExchange, HTTP client interface, Spring HTTP Interface, or wants to create/modify/test HTTP client
  proxies. Also trigger when users ask about Spring 6 HTTP Interface auto-configuration, load-balanced HTTP clients,
  switching between reactive and sync HTTP client modes, or reference classes like CoApiFactoryBean, CoApiDefinition,
  ClientMode, WebClientFactoryBean, RestClientFactoryBean, HttpExchangeAdapterFactory, AutoCoApiRegistrar,
  or configuration properties like coapi.mode, coapi.clients, coapi.base-packages. Make sure to use this skill even
  if the user doesn't explicitly mention CoApi by name but is working with Spring HTTP Interface proxies, declaring
  annotated HTTP client interfaces, or configuring Spring Boot auto-configuration for HTTP exchanges.
---

# CoApi Developer Skill

You are helping a developer use the **CoApi** library (`me.ahoo.coapi`), which provides zero-boilerplate auto-configuration for Spring 6 HTTP Interface clients (`@HttpExchange`). It supports both reactive (`WebClient`) and synchronous (`RestClient`) modes, with optional client-side load balancing via Spring Cloud LoadBalancer.

CoApi targets Spring Boot 4.x / Spring Framework 7.x with Kotlin (JVM 17).

## How to Respond

- Generate working, copy-pasteable code snippets — not pseudocode
- When multiple approaches exist, recommend the simplest one first and explain the tradeoffs
- If the developer's request goes beyond what CoApi handles (e.g., retry logic, circuit breaking, WebSocket), say so clearly and suggest the right tool for that concern

## Quick Reference

**Maven coordinates:**
```kotlin
implementation("me.ahoo.coapi:coapi-spring-boot-starter")
```

Note: The Maven artifact ID is `coapi-spring-boot-starter` (not `spring-boot-starter`). The module name in the project is `spring-boot-starter`, but the published artifact follows the pattern `coapi-<module-name>` via `getArchivesName()`.

**Key annotations:**
- `@CoApi(baseUrl | serviceId, name)` — marks an interface as an HTTP client proxy
- `@LoadBalanced` — enables load balancing for the client
- `@EnableCoApi(clients = [...])` — explicitly registers client interfaces (alternative to auto-scanning)

**Configuration property:** `coapi.mode` = `AUTO` | `REACTIVE` | `SYNC`

## When NOT to Use CoApi

CoApi is designed specifically for declarative HTTP Interface clients. It is not the right choice for:

- **WebSocket or SSE clients** — CoApi only supports request-response HTTP. For streaming protocols, use `WebSocketClient` or `WebClient` directly.
- **Fine-grained retry / circuit breaking** — CoApi delegates HTTP calls to `WebClient` or `RestClient`, so resilience features belong in filters/interceptors (e.g., Resilience4j). CoApi configures the wiring, not the resilience policy.
- **Non-Spring applications** — CoApi relies on Spring Boot auto-configuration and bean registration. For standalone use, call Spring's `HttpServiceProxyFactory` directly.
- **Arbitrary HTTP calls without an interface** — If you just need to make one-off HTTP requests, use `RestClient` or `WebClient` directly. CoApi adds value when you have a typed interface contract.

## Creating @CoApi Interfaces

### 1. Define the API interface

Use standard Spring `@HttpExchange` annotations on the interface. A class-level `@HttpExchange` sets a base path for all methods — this is useful for grouping endpoints under a common prefix. Return types determine the programming model:
- **Reactive**: `Flux<T>`, `Mono<T>`, or any `Publisher<T>`
- **Sync**: plain Java/Kotlin types (`List<T>`, `T`, `void`)

```kotlin
@HttpExchange("todo")
interface TodoApi {
    @GetExchange
    fun getTodo(): Flux<Todo>
}

data class Todo(val title: String)
```

### 2. Create the @CoApi client

The `@CoApi` annotation goes on the client interface. There are several ways to specify the target:

**Direct URL (with optional placeholder):**
```kotlin
@CoApi(baseUrl = "\${github.url}")
interface GitHubApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssue(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}
```

**Service ID (load-balanced):**
When you set `serviceId`, CoApi automatically constructs an `lb://serviceId` URL. This tells Spring Cloud LoadBalancer to resolve the actual host from the service registry — you don't need to know the concrete URL at development time.
```kotlin
@CoApi(serviceId = "github-service")
interface ServiceApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssue(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}
```

**Load-balanced URL:**
```kotlin
@CoApi(baseUrl = "lb://order-service")
interface OrderClient
```

**Load-balanced with @LoadBalanced annotation:**
Use `@LoadBalanced` when you want to explicitly mark a client for load balancing regardless of the URL scheme — for example, when the base URL is a plain `https://` address that should still go through the load balancer.
```kotlin
@CoApi(baseUrl = "https://order-service")
@LoadBalanced
interface OrderClient {
    @GetExchange("/orders/{id}")
    fun getOrder(@PathVariable id: String): Order
}

data class Order(val id: String, val status: String)
```

Requires `spring-cloud-starter-loadbalancer` on classpath.

**Extending a shared API interface:**
This pattern is useful in microservice architectures where the API contract is defined in a shared module — the provider and consumer both depend on the same interface.
```kotlin
@CoApi(serviceId = "provider-service")
interface TodoClient : TodoApi
```

**No base URL (resolved from config):**
When no `baseUrl` or `serviceId` is set, the base URL must come from `coapi.clients.<name>.baseUrl` in application config. This is useful when the target URL varies per environment and should not be hardcoded in the annotation.
```kotlin
@CoApi
interface ConfigResolvedClient {
    @GetExchange("/users")
    fun getUsers(): Flux<User>
}
```

### 3. Resolve base URL

Base URL resolution priority (first match wins):
1. `@CoApi(baseUrl = "...")` — supports `${property}` placeholders resolved at startup
2. `@CoApi(serviceId = "...")` — auto-constructs `lb://serviceId`
3. `coapi.clients.<name>.baseUrl` in application config — runtime override per client

Do not set both `baseUrl` and `serviceId` on the same `@CoApi` — the behavior is undefined.

## Spring Boot Auto-Configuration

Adding the starter dependency triggers auto-configuration of:
- Classpath scanning for `@CoApi`-annotated interfaces
- `WebClient` or `RestClient` bean creation per client
- HTTP service proxy creation via `HttpServiceProxyFactory`

### Client Mode

Set via `coapi.mode` in `application.yml`:

```yaml
coapi:
  mode: AUTO  # AUTO | REACTIVE | SYNC
```

- `AUTO` (default): detects reactive Web stack on classpath → uses `WebClient`; otherwise uses `RestClient`. The detection works by checking for `org.springframework.web.reactive.HandlerResult`, which is present whenever `spring-webflux` is on the classpath.
- `REACTIVE`: forces `WebClient`-based adapters
- `SYNC`: forces `RestClient`-based adapters

### Scan Base Packages

By default, scans the Spring Boot application's base package. Override with:

```yaml
coapi:
  base-packages:
    - com.example.clients
    - com.other.api
```

### Per-Client Configuration

Override individual client settings in `application.yml`:

```yaml
coapi:
  clients:
    GitHubApiClient:
      base-url: https://api.github.com
      load-balanced: false
      reactive:
        filter:
          names:
            - loadBalancerExchangeFilterFunction
      sync:
        interceptor:
          names:
            - loadBalancerInterceptor
```

### Explicit Registration

If auto-scanning doesn't fit (e.g., client interfaces live in an external library JAR), use `@EnableCoApi` to list clients explicitly:

```kotlin
@EnableCoApi(
    clients = [
        GitHubApiClient::class,
        ServiceApiClient::class,
        TodoClient::class
    ]
)
@SpringBootApplication
class MyApp
```

### Load Balancing

Requires `spring-cloud-starter-loadbalancer` on classpath:

```kotlin
implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
```

Then configure filters/interceptors per client mode:

**Reactive (WebClient):** Set filter by bean name or type in `coapi.clients.<name>.reactive.filter`
**Sync (RestClient):** Set interceptor by bean name or type in `coapi.clients.<name>.sync.interceptor`

## Writing Tests

Tests use `me.ahoo.test:fluent-assert-core` for assertions — use the `.assert()` extension, not AssertJ's `assertThat()`. This provides null-safe, Kotlin-idiomatic assertions.

```kotlin
import me.ahoo.test.asserts.assert
```

### Unit Tests for CoApiDefinition

Test annotation parsing with `MockEnvironment`:

```kotlin
class CoApiDefinitionTest {
    @Test
    fun toCoApiDefinitionIfServiceApi() {
        val coApiDefinition = MockServiceApi::class.java.toCoApiDefinition(MockEnvironment())
        coApiDefinition.loadBalanced.assert().isTrue()
        coApiDefinition.baseUrl.assert().isEqualTo("http://order-service")
    }
}

@CoApi(serviceId = "order-service")
interface MockServiceApi
```

### Integration Tests with ApplicationContextRunner

Use `ApplicationContextRunner` to test the full bean registration:

```kotlin
class CoApiContextTest {
    @Test
    fun `should create Reactive CoApi bean`() {
        ApplicationContextRunner()
            .withPropertyValues("github.url=https://api.github.com")
            .withBean(WebClientBuilderCustomizer::class.java, { WebClientBuilderCustomizer.NoOp })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(EnableCoApiConfiguration::class.java)
            .run { context ->
                context.assert()
                    .hasSingleBean(ReactiveHttpExchangeAdapterFactory::class.java)
                    .hasSingleBean(GitHubApiClient::class.java)
            }
    }
}

@EnableCoApi(clients = [GitHubApiClient::class])
class EnableCoApiConfiguration
```

### Spring Boot Integration Tests

```kotlin
@SpringBootTest
class ConsumerServerTest {
    @Autowired
    private lateinit var gitHubApiClient: GitHubApiClient

    @Test
    fun getIssueByGitHubApiClient() {
        gitHubApiClient.getIssue("Ahoo-Wang", "Wow")
            .doOnNext { println(it) }
            .blockLast()
    }
}
```

### Testing with MockK

Use MockK for mocking client interfaces:

```kotlin
@Test
fun `should mock CoApi client`() {
    val mockClient = mockk<GitHubApiClient>()
    every { mockClient.getIssue("owner", "repo") } returns Flux.just(Issue("url"))
    // ... test with mockClient
}
```

## Common Patterns

### Sync vs Reactive in the Same Interface

A single interface can mix sync and reactive return types. CoApi's `HttpServiceProxyFactory` detects the return type per method and uses the appropriate adapter — you don't need separate interfaces for sync and reactive.

```kotlin
@CoApi(baseUrl = "\${github.url}")
interface GitHubSyncClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssue(@PathVariable owner: String, @PathVariable repo: String): List<Issue>  // sync

    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssueWithReactive(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>  // reactive
}
```

### Dynamic URI with UriBuilderFactory

Pass `UriBuilderFactory` or `URI` directly for dynamic URL resolution — useful when the target host is determined at runtime:

```kotlin
@CoApi
interface UriApiClient {
    @GetExchange
    fun getIssueByUri(uri: URI): Flux<Issue>

    @GetExchange
    fun getIssue(
        uriBuilderFactory: UriBuilderFactory,
        @PathVariable owner: String,
        @PathVariable repo: String
    ): Flux<Issue>
}
```

## Troubleshooting

### Client bean not found

This typically manifests as `NoSuchBeanDefinitionException` at startup.

- Check that `@CoApi` is on an **interface**, not a class — CoApi uses Java dynamic proxies, which only work with interfaces
- Verify the interface is in a scanned package (or listed in `@EnableCoApi`)
- Check `coapi.base-packages` if using custom packages

### Load balancing not working

Requests go to a single instance or fail with connection refused.

- Ensure `spring-cloud-starter-loadbalancer` is on classpath
- Verify `serviceId` is set (or `baseUrl` uses `lb://` prefix), or `@LoadBalanced` is on the interface
- Configure the appropriate filter (reactive) or interceptor (sync) in `coapi.clients.<name>`

### Wrong client mode

The application uses `RestClient` when you expected `WebClient`, or vice versa.

- Check `coapi.mode` property
- `AUTO` mode: verify if `org.springframework.web.reactive.HandlerResult` is on classpath — it's present when `spring-webflux` is a dependency
- Explicitly set `REACTIVE` or `SYNC` if auto-detection is wrong

### Base URL not resolving

Requests fail with "no base URL configured" or go to the wrong host.

- Property placeholders (`${...}`) require the property to exist in `application.yml` — a missing property causes startup failure
- `serviceId` auto-constructs `lb://serviceId` — don't also set `baseUrl`, the two are mutually exclusive
- Per-client `coapi.clients.<name>.baseUrl` overrides annotation values
- When no `baseUrl` or `serviceId` is set on `@CoApi`, the base URL must come from config

## Key Implementation Details

- `@CoApi` is meta-annotated with `@Component`, so interfaces are Spring beans and can be `@Autowired` directly
- Bean names: `{name}.HttpClient` for the HTTP client, `{name}.CoApi` for the proxy
- `CoApiFactoryBean` creates proxies via Spring's `HttpServiceProxyFactory`
- `ClientMode.inferClientMode()` checks for reactive class on classpath when mode is `AUTO`
- `AutoCoApiRegistrar` (boot) or `EnableCoApiRegistrar` handles classpath scanning and bean registration
