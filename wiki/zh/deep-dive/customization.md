---
title: 自定义
description: 单客户端过滤器与拦截器、全局 Builder 定制器、替换 CoApi 的 Bean，以及客户端的构建顺序。
---

# 自定义

CoApi 基于应用的 Builder Bean 创建每个 HTTP 客户端。你可以通过三种方式介入，作用范围从小到大：

| 需求 | 使用 |
|------|------|
| 给**部分**客户端添加过滤器/拦截器，无需代码 | `coapi.clients.<name>.reactive.filter` / `.sync.interceptor` |
| 修改**所有**客户端的 Builder，或在代码中按客户端修改 | `WebClientBuilderCustomizer` / `RestClientBuilderCustomizer` Bean |
| 完全替换某个客户端或适配器 | 自己定义对应的 Bean |

## 客户端的构建过程

对每个客户端，`WebClientFactoryBean` 或 `RestClientFactoryBean` 会：

1. 从容器获取 Builder（`WebClient.Builder` / `RestClient.Builder`）；
2. 设置[生效定义](../getting-started/configuration.md#覆盖规则)中的 base URL；
3. 添加配置的过滤器（响应式）或拦截器（同步）：先按名称，再按类型；
4. 如果客户端是[负载均衡](./load-balancing.md)的，添加负载均衡器；
5. 按 `@Order` 顺序调用所有 Builder 定制器 Bean；
6. 构建客户端。

定制器最后运行，因此能看到并修改之前的所有设置。

## 单客户端过滤器与拦截器

把过滤器或拦截器声明为 Bean，然后按 Bean 名称或类型引用：

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

引用了不存在的 Bean 会导致客户端创建失败。`types` 中的条目必须恰好匹配一个 Bean。

## Builder 定制器

定制器 Bean 作用于其模式下的所有客户端。它接收客户端的生效定义，因此可以根据 `name`、`baseUrl` 或 `loadBalanced` 区别处理：

```kotlin
@Component
class ConnectionPoolCustomizer : WebClientBuilderCustomizer {
    override fun customize(coApiDefinition: CoApiDefinition, builder: WebClient.Builder) {
        val provider = ConnectionProvider.builder(coApiDefinition.name)   // 每个客户端一个连接池
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

只有当前模式的定制器会被调用。如果设置应作用于应用中的所有 Builder（而不仅是 CoApi 的），请使用 Spring Boot 自带的 `WebClientCustomizer` / `RestClientCustomizer`。

FactoryBean 是 final 的。原本想通过继承实现的逻辑，请放到定制器中。

## 替换 Bean

CoApi 只在不存在同名 Bean 定义时才注册 Bean，因此你可以抢先定义：

| Bean 名称 | 替换它以便 |
|-----------|------------|
| `<name>.HttpClient` | 为某个客户端提供手工构建的 `WebClient` / `RestClient` |
| `<name>.CoApi` | 提供接口的自定义实现 |
| `CoApi.HttpExchangeAdapterFactory` | 控制如何从 HTTP 客户端创建 `HttpExchangeAdapter`（作用于所有客户端） |

自定义的 `HttpExchangeAdapterFactory` 需以 `CoApi.HttpExchangeAdapterFactory` 为名称注册，或标记为 `@Primary`，才会生效：

```kotlin
@Bean(HttpExchangeAdapterFactory.BEAN_NAME)
fun httpExchangeAdapterFactory() = HttpExchangeAdapterFactory { beanFactory, httpClientName ->
    val webClient = beanFactory.getBean(httpClientName, WebClient::class.java)
    WebClientAdapter.create(webClient).apply { blockTimeout = Duration.ofSeconds(5) }
}
```

只有当你的 Bean 定义在 CoApi 注册器运行之前注册时，替换才会生效。你自己的 `@Configuration` 类中的 Bean 定义满足这一点，因为自动配置在它们之后处理。能用定制器解决时，优先使用定制器。
