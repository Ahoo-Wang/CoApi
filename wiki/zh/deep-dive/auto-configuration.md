---
title: 注册客户端
description: CoApi 如何发现客户端接口（Spring Boot classpath 扫描、coapi.base-packages、@EnableCoApi 或 CoApiDefinition Bean），以及如何在没有 Spring Boot 时使用。
---

# 注册客户端

`@CoApi` 接口只有被注册器发现后才会成为 Bean。有四种方式确保这一点，它们可以组合使用。

| 方式 | 需要 Spring Boot | 适用于 |
|------|------------------|--------|
| classpath 扫描（默认） | 是 | 应用自身包中的客户端 |
| `coapi.base-packages` | 是 | 其他包或 JAR 中的客户端，按包注册 |
| `@EnableCoApi(clients = [...])` | 否 | 显式列出的客户端，可位于任意包 |
| `CoApiDefinition` Bean | 是 | 无法添加注解的接口，或在代码中构建的定义 |

## classpath 扫描

starter 在 classpath 上时，`CoApiAutoConfiguration` 会在**自动配置包**（`@SpringBootApplication` 类所在的包及其子包）中扫描标注了 `@CoApi` 的接口。类会被忽略。

除非设置了 `coapi.enabled=false`，扫描始终开启。

## `coapi.base-packages`

添加额外扫描的包，通常用于库 JAR 中提供的客户端：

```yaml
coapi:
  base-packages:
    - com.example.order.client
    - com.example.payment.client
```

也支持逗号分隔的字符串（`coapi.base-packages=com.a,com.b`）。

## `@EnableCoApi`

精确注册列出的接口：

```kotlin
@EnableCoApi(clients = [TodoClient::class, GitHubApiClient::class])
@SpringBootApplication
class ConsumerApplication
```

`@EnableCoApi` 位于 `coapi-spring` 中，不依赖 Spring Boot，也不受 `coapi.enabled` 影响。

通过多种方式注册同一个接口是无害的。先注册的生效，后续注册会被跳过并输出一条 `WARN` 日志。

## `CoApiDefinition` Bean

无法给接口加 `@CoApi`（例如来自第三方，或定义需要计算得出）时，可以声明一个 `CoApiDefinition` Bean：

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
            loadBalanced = false, // lb:// 已启用负载均衡
        )
    }
}
```

规则：

- `@Bean` 方法必须是**静态**的（Kotlin：放在 `companion object` 中并加 `@JvmStatic`）。CoApi 在 Bean 后处理之前读取这些 Bean。非静态方法会过早创建它所在的配置类，因此启动会直接失败。
- `baseUrl` 中的占位符和 `lb://` 与注解中的处理方式相同。
- 名称在扫描、显式列出和定义 Bean 的所有客户端之间必须唯一。
- 只有 Spring Boot 自动配置会收集这些 Bean。仅使用 `@EnableCoApi` 时不会收集。

## 不使用 Spring Boot

使用 `coapi-spring`，在配置类上标注 `@EnableCoApi`，并自行提供原本由 Boot 提供的内容：

```kotlin
@Configuration
@EnableCoApi(clients = [GitHubApiClient::class])
class ClientConfiguration {
    @Bean
    @Scope("prototype")
    fun restClientBuilder(): RestClient.Builder = RestClient.builder()
}
```

- 与[客户端模式](./client-modes.md)匹配的 `WebClient.Builder` 或 `RestClient.Builder` Bean。CoApi 会修改它拿到的 Builder，因此应使用 prototype 作用域。
- `coapi.mode` 从 `Environment` 读取。请显式设置，因为 `AUTO` 可能选中你没有定义 Builder 的模式。
- 可选的 `ClientProperties` / `ReactiveClientProperties` / `SyncClientProperties` Bean，用于单客户端覆盖。见[配置参考](../getting-started/configuration.md#不使用-spring-boot)。
