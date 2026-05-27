# CoApi Usage Patterns

Load this reference when creating client interfaces, dependency snippets, application configuration, or
load-balanced examples.

## Contents

- [Dependency](#dependency)
- [Define The API Contract](#define-the-api-contract)
- [Direct URL Client](#direct-url-client)
- [Service Discovery Client](#service-discovery-client)
- [Load-Balanced URL Or Annotation](#load-balanced-url-or-annotation)
- [Shared Provider And Consumer Contract](#shared-provider-and-consumer-contract)
- [Config-Resolved Client](#config-resolved-client)
- [Client Mode](#client-mode)
- [Per-Client Base URL Override](#per-client-base-url-override)
- [Per-Client Load-Balancer Wiring](#per-client-load-balancer-wiring)
- [Explicit Registration](#explicit-registration)
- [Dynamic URI](#dynamic-uri)

## Dependency

```kotlin
implementation("me.ahoo.coapi:coapi-spring-boot-starter")
```

Use the published artifact `coapi-spring-boot-starter`; the repository module is named
`spring-boot-starter`, but artifacts are published with the `coapi-` prefix.

## Define The API Contract

Use Spring `@HttpExchange` annotations on the interface. Return types guide the programming model:

- Reactive: `Flux<T>`, `Mono<T>`, or another `Publisher<T>`.
- Sync: plain JVM values such as `T`, `List<T>`, or `Unit`/`void`.

```kotlin
@HttpExchange("todo")
interface TodoApi {
    @GetExchange
    fun getTodo(): Flux<Todo>
}

data class Todo(val title: String)
```

## Direct URL Client

```kotlin
@CoApi(baseUrl = "\${github.url}")
interface GitHubApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssue(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}

data class Issue(val url: String)
```

```yaml
github:
  url: https://api.github.com
```

## Service Discovery Client

Use `serviceId` when the target is registered with service discovery. CoApi builds `lb://<serviceId>`.

```kotlin
@CoApi(serviceId = "user-service")
interface UserClient {
    @GetExchange("/users/{id}")
    fun getUser(@PathVariable id: String): Mono<User>

    @GetExchange("/users")
    fun listUsers(): Flux<User>
}

data class User(val id: String, val name: String)
```

Add load-balancer support:

```kotlin
implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
```

## Load-Balanced URL Or Annotation

Use an `lb://` URL when the service name belongs in configuration-like annotation data:

```kotlin
@CoApi(baseUrl = "lb://order-service")
interface OrderClient
```

Use `@LoadBalanced` when the annotation should explicitly request load-balanced wiring:

```kotlin
@CoApi(baseUrl = "https://order-service")
@LoadBalanced
interface OrderClient {
    @GetExchange("/orders/{id}")
    fun getOrder(@PathVariable id: String): Order
}
```

## Shared Provider And Consumer Contract

Put the shared API interface in a common module, implement it on the provider, and extend it on the
consumer client.

```kotlin
@HttpExchange("todo")
interface TodoApi {
    @GetExchange
    fun getTodo(): Flux<Todo>
}

@CoApi(serviceId = "provider-service")
interface TodoClient : TodoApi

@RestController
class TodoController : TodoApi {
    override fun getTodo(): Flux<Todo> =
        Flux.range(1, 10).map { Todo("todo-$it") }
}
```

## Config-Resolved Client

When no URL belongs in source code, leave `@CoApi` empty and configure the named client externally.

```kotlin
@CoApi
interface ConfigResolvedClient {
    @GetExchange("/users")
    fun getUsers(): Flux<User>
}
```

```yaml
coapi:
  clients:
    ConfigResolvedClient:
      base-url: https://api.example.com
```

## Client Mode

```yaml
coapi:
  mode: AUTO # AUTO | REACTIVE | SYNC
```

Use `REACTIVE` for `WebClient`, `SYNC` for `RestClient`, and `AUTO` when classpath inference is
acceptable.

## Per-Client Base URL Override

`coapi.clients.<name>.base-url` overrides the URL derived from `@CoApi(baseUrl)` or `serviceId` when
the HTTP client factory builds the backing client.

```yaml
coapi:
  clients:
    GitHubApiClient:
      base-url: https://api.github.com
```

## Per-Client Load-Balancer Wiring

```yaml
coapi:
  clients:
    UserClient:
      reactive:
        filter:
          names:
            - loadBalancerExchangeFilterFunction
      sync:
        interceptor:
          names:
            - loadBalancerInterceptor
```

## Explicit Registration

Use `@EnableCoApi` when auto-scanning does not cover the client package.

```kotlin
@EnableCoApi(
    clients = [
        GitHubApiClient::class,
        UserClient::class,
        TodoClient::class
    ]
)
@SpringBootApplication
class ConsumerApplication
```

## Dynamic URI

Accept `URI` or `UriBuilderFactory` in the interface method when the target is fully dynamic.

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
