---
title: Defining Clients
description: The @CoApi and @LoadBalanced annotations, base URL resolution, client naming, and patterns for shared contracts and dynamic URIs.
---

# Defining Clients

A CoApi client is an **interface** that has Spring `@HttpExchange` methods and is marked with `@CoApi`. Everything about the request (paths, methods, headers, bodies) is standard Spring [HTTP Interface](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface). CoApi only adds where the requests go and how the client is wired.

## `@CoApi`

| Attribute | Default | Meaning |
|-----------|---------|---------|
| `baseUrl` | `""` | Base URL of the target, e.g. `https://api.github.com`, `${github.url}` or `lb://order-service`. Placeholders are resolved at startup and must exist. |
| `serviceId` | `""` | Service discovery ID. Used only when `baseUrl` is blank, as `lb://<serviceId>`. Placeholders allowed. |
| `name` | interface simple name | Client name: used in bean names and as the `coapi.clients.<name>` key. Must be unique. |

```kotlin
@CoApi(baseUrl = "\${github.url}")            // fixed endpoint from configuration
interface GitHubApiClient

@CoApi(serviceId = "order-service")           // discovered service, load balanced
interface OrderClient

@CoApi(baseUrl = "lb://order-service")        // same as serviceId
interface OrderClientViaUrl

@CoApi(name = "GitHubApi", serviceId = "github-service")  // custom name
interface ServiceApiClient
```

Set either `baseUrl` or `serviceId`, not both. If both are set, `baseUrl` wins.

## Base URL resolution

1. A non-blank `baseUrl` is used, with placeholders resolved.
2. Otherwise a non-blank `serviceId` becomes `lb://<serviceId>`.
3. Otherwise the base URL is empty.

Then, if the URL starts with `lb://` (case-insensitive), CoApi rewrites it to `http://` and marks the client **load balanced**. Spring Cloud LoadBalancer later replaces the host with a real instance. See [Load Balancing](./load-balancing.md).

`coapi.clients.<name>.base-url` and `.load-balanced` can override all of this per environment. See the [override rules](../getting-started/configuration.md#override-rules).

## `@LoadBalanced`

`me.ahoo.coapi.api.LoadBalanced` (not Spring Cloud's annotation of the same name) marks a client as load balanced while keeping a plain URL:

```kotlin
@CoApi(baseUrl = "http://order-service")
@LoadBalanced
interface OrderClient
```

This is equivalent to `@CoApi(serviceId = "order-service")`. It is useful when the URL comes from a placeholder that should not contain `lb://`.

## Client name and beans

Each client gets two beans named after it:

| Bean | Type | Purpose |
|------|------|---------|
| `<name>.HttpClient` | `WebClient` or `RestClient` | The underlying HTTP client |
| `<name>.CoApi` | your interface | The proxy you inject |

Inject the interface by type. Use the `WebClient`/`RestClient` bean by name only when you need the raw client for the same target.

Two clients with the same name fail startup (`Duplicate CoApi name`). This happens when interfaces in different packages share a simple name; give one a `name`.

## No base URL

A client without a base URL can still be used: pass the target per request as a `URI` or `UriBuilderFactory` argument, which Spring's HTTP Interface supports natively.

```kotlin
@CoApi
interface UriApiClient {
    @GetExchange
    fun getIssueByUri(uri: URI): Flux<Issue>

    @GetExchange
    fun getIssue(
        uriBuilderFactory: UriBuilderFactory,
        @PathVariable owner: String,
        @PathVariable repo: String,
    ): Flux<Issue>
}
```

You can also leave the URL out of the code and supply it with `coapi.clients.UriApiClient.base-url`.

## Shared contracts

A provider and its consumers can share one interface, so the server implementation and the client cannot drift apart:

```kotlin
// provider-api module: plain Spring HTTP Interface
@HttpExchange("todo")
interface TodoApi {
    @GetExchange
    fun getTodo(): Flux<Todo>
}

// provider-api module: the client, depends only on coapi-api
@CoApi(serviceId = "provider-service")
interface TodoClient : TodoApi

// provider server: implements the contract
@RestController
class TodoController : TodoApi {
    override fun getTodo(): Flux<Todo> = Flux.range(1, 10).map { Todo("todo-$it") }
}
```

Consumers depend on the API module and inject `TodoClient`. Because the client lives in the provider's package, consumers register it explicitly, either with `@EnableCoApi(clients = [TodoClient::class])` or `coapi.base-packages`. See [Registering Clients](./auto-configuration.md).
