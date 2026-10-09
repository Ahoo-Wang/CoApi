---
title: 迁移到 3.0
description: CoApi 3.0.0 的破坏性变更以及从 2.x 迁移的方法。YAML 配置不变，只影响 Kotlin/Java 扩展 SPI 和 CoApiDefinition Bean。
---

# 迁移到 3.0

CoApi 3.0.0 重新划分了客户端 SPI，让每个类型只承担一项职责。**只使用 `@CoApi`、`@EnableCoApi` 和 `coapi.*` YAML 配置的应用无需任何改动**：所有配置键的名称和含义都保持不变。

只有以下情况会受影响：

- 在代码中实现或引用了 `ClientProperties`、`FilterDefinition` 或 `InterceptorDefinition`；
- 继承或引用了 `AbstractWebClientFactoryBean`、`AbstractRestClientFactoryBean` 或 `AbstractHttpClientFactoryBean`；
- 声明了 `CoApiDefinition` Bean。

## 客户端配置 SPI

`ClientProperties` 只保留与模式无关的端点覆盖配置。过滤器和拦截器分别移到响应式和同步包下的角色接口中，两种定义类型统一为泛型的 `ComponentDefinition<T>`。

| 2.x | 3.0 |
|-----|-----|
| `ClientProperties.getBaseUri` / `getLoadBalanced` | 不变，新增 `resolve(definition)` |
| `ClientProperties.getFilter(name): FilterDefinition` | `ReactiveClientProperties.getFilter(name): ComponentDefinition<ExchangeFilterFunction>` |
| `ClientProperties.getInterceptor(name): InterceptorDefinition` | `SyncClientProperties.getInterceptor(name): ComponentDefinition<ClientHttpRequestInterceptor>` |
| `ClientProperties.FilterDefinition` / `InterceptorDefinition` | `me.ahoo.coapi.spring.client.ComponentDefinition<T>` |

```kotlin
// 2.x
class MyClientProperties : ClientProperties {
    override fun getBaseUri(coApiName: String) = ""
    override fun getLoadBalanced(coApiName: String): Boolean? = null
    override fun getFilter(coApiName: String) = ClientProperties.FilterDefinition(names = listOf("authFilter"))
    override fun getInterceptor(coApiName: String) = ClientProperties.InterceptorDefinition()
}

// 3.0 —— 只实现需要的角色接口，每个都是可选 Bean
class MyClientProperties : ClientProperties, ReactiveClientProperties {
    override fun getBaseUri(coApiName: String) = ""
    override fun getLoadBalanced(coApiName: String): Boolean? = null
    override fun getFilter(coApiName: String) = ComponentDefinition<ExchangeFilterFunction>(names = listOf("authFilter"))
}
```

不使用 Spring Boot 时，每个角色接口都是可选的，缺失时回退到各自的 `Empty` 实现；使用 Spring Boot 时，`CoApiProperties` 实现了全部三个接口。

## 工厂 Bean

`AbstractWebClientFactoryBean` 和 `AbstractRestClientFactoryBean` 已移除。`WebClientFactoryBean` 和 `RestClientFactoryBean` 现在是 final 类，负载均衡定制器改为内部类。`AbstractHttpClientFactoryBean` 变为泛型类，原有的属性读取方法（`getBaseUrl()`、`loadBalanced()`、`getBaseUrlFromProperties()`、`getLoadBalancedFromProperties()`）由 `effectiveDefinition()` 取代。

请把工厂 Bean 子类中的逻辑移到定制器 Bean 中：

```kotlin
@Component
class TimeoutCustomizer : WebClientBuilderCustomizer {
    override fun customize(coApiDefinition: CoApiDefinition, builder: WebClient.Builder) {
        // coApiDefinition 是*生效的*定义：已应用 coapi.clients.<name>.* 覆盖配置
    }
}
```

定制器现在收到的是**生效的**定义。在 2.x 中它们收到的是注解定义，因此 `coApiDefinition.baseUrl` 和 `coApiDefinition.loadBalanced` 可能与客户端实际使用的值不一致。

## CoApiDefinition Bean

由**非静态** `@Bean` 方法声明的 `CoApiDefinition` Bean 现在会导致启动失败（2.3.0 中只是警告）。这种方法会迫使所在的配置类在 Bean 后置处理之前被创建，悄无声息地跳过 `@Autowired`/`@Value` 注入和 `@Bean` 代理。请把方法声明为静态，并让 CoApi 像处理注解一样解析 `baseUrl` 中的占位符和 `lb://`：

```kotlin
@Configuration
class OrderApiConfiguration {
    companion object {
        @JvmStatic
        @Bean
        fun orderApiDefinition(): CoApiDefinition = CoApiDefinition(
            name = "OrderApi",
            apiType = OrderApi::class.java,
            baseUrl = "lb://\${order.service-id}",
            loadBalanced = false, // lb:// 会启用负载均衡
        )
    }
}
```

## 新增的快速失败检查

- 被判定为负载均衡、但 classpath 上没有 Spring Cloud LoadBalancer 的客户端，会抛出指明客户端和解决办法的异常，而不是 `NoClassDefFoundError`。

## 相关页面

- [配置参考](./configuration.md)
- [自定义配置](../deep-dive/customization.md)
- [负载均衡](../deep-dive/load-balancing.md)
- [自动配置](../deep-dive/auto-configuration.md)
