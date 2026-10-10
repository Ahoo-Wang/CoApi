---
title: Customization
description: Per-client filters and interceptors, global builder customizers, replacing CoApi beans, and the order in which a client is built.
---

# Customization

CoApi creates each HTTP client from the application's builder bean. You can hook into that in three ways, from narrowest to broadest:

| Need | Use |
|------|-----|
| Add a filter/interceptor to **some** clients, no code | `coapi.clients.<name>.reactive.filter` / `.sync.interceptor` |
| Change the builder of **every** client, or per client in code | `WebClientBuilderCustomizer` / `RestClientBuilderCustomizer` beans |
| Replace a client or the adapter entirely | Define the bean yourself |

## How a client is built

For each client, `WebClientFactoryBean` or `RestClientFactoryBean`:

1. gets a builder from the context (`WebClient.Builder` / `RestClient.Builder`);
2. sets the base URL of the [effective definition](../getting-started/configuration.md#override-rules);
3. adds the configured filters (reactive) or interceptors (sync): names first, then types;
4. adds the load balancer if the client is [load balanced](./load-balancing.md);
5. calls every builder customizer bean, in `@Order` order;
6. builds the client.

Customizers run last, so they see and can change everything before them.

## Per-client filters and interceptors

Declare the filter or interceptor as a bean, then reference it by bean name or by type:

```kotlin
@Bean
fun tenantHeaderFilter() = ExchangeFilterFunction { request, next ->
    next.exchange(ClientRequest.from(request).header("X-Tenant", "acme").build())
}
```

```yaml
coapi:
  clients:
    GitHubApiClient:
      reactive:
        filter:
          names: [tenantHeaderFilter]
    PaymentClient:
      sync:
        interceptor:
          types: [com.example.PaymentAuthInterceptor]
```

A referenced bean that doesn't exist fails client creation. A `types` entry must resolve to exactly one bean.

## Builder customizers

A customizer bean applies to every client of its mode. It receives the client's effective definition, so it can branch on `name`, `baseUrl` or `loadBalanced`:

```kotlin
@Component
class ConnectionPoolCustomizer : WebClientBuilderCustomizer {
    override fun customize(coApiDefinition: CoApiDefinition, builder: WebClient.Builder) {
        val provider = ConnectionProvider.builder(coApiDefinition.name)   // one pool per client
            .maxConnections(500)
            .maxIdleTime(Duration.ofSeconds(20))
            .build()
        builder.clientConnector(ReactorClientHttpConnector(HttpClient.create(provider)))
    }
}
```

```kotlin
@Component
class TimeoutCustomizer : RestClientBuilderCustomizer {
    override fun customize(coApiDefinition: CoApiDefinition, builder: RestClient.Builder) {
        if (coApiDefinition.name != "SlowReportClient") return
        val factory = SimpleClientHttpRequestFactory().apply { setReadTimeout(Duration.ofSeconds(30)) }
        builder.requestFactory(factory)
    }
}
```

Only customizers of the active mode are called. Use Spring Boot's own `WebClientCustomizer` / `RestClientCustomizer` for settings that should apply to every builder in the application, not only CoApi's.

The factory beans are final. Put any logic you would have added by subclassing into a customizer.

## Replacing beans

CoApi registers a bean only if no bean definition with that name exists yet. You can therefore pre-empt it:

| Bean name | Replace it to |
|-----------|---------------|
| `<name>.HttpClient` | Supply a hand-built `WebClient` / `RestClient` for one client |
| `<name>.CoApi` | Supply your own implementation of the interface |
| `CoApi.HttpExchangeAdapterFactory` | Control how the `HttpExchangeAdapter` is created from the HTTP client, for all clients |

A custom `HttpExchangeAdapterFactory` takes effect if it is registered under the name `CoApi.HttpExchangeAdapterFactory` or marked `@Primary`:

```kotlin
@Bean(HttpExchangeAdapterFactory.BEAN_NAME)
fun httpExchangeAdapterFactory() = HttpExchangeAdapterFactory { beanFactory, httpClientName ->
    val webClient = beanFactory.getBean(httpClientName, WebClient::class.java)
    WebClientAdapter.create(webClient).apply { blockTimeout = Duration.ofSeconds(5) }
}
```

Replacing beans only works if your definition is registered before CoApi's registrar runs. Bean definitions from your own `@Configuration` classes are, because auto-configuration is processed after them. Prefer customizers when they are enough.
