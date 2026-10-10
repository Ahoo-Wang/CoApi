---
title: 什么是 CoApi？
description: CoApi 把 Spring HTTP Interface（@HttpExchange）接口声明变成可注入的 Spring Bean，底层使用 WebClient 或 RestClient，并可选支持客户端负载均衡。
---

# 什么是 CoApi？

Spring 的 [HTTP Interface](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface) 允许用带 `@HttpExchange` 方法的 Java/Kotlin 接口来声明 HTTP API。但要真正调用它，仍需为每个接口构建 HTTP 客户端、包装成适配器、用 `HttpServiceProxyFactory` 创建代理，再把代理注册为 Bean。

CoApi 替你完成这些装配工作。给接口加上 `@CoApi`，即可直接注入：

```kotlin
@CoApi(baseUrl = "\${github.url}")
interface GitHubApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssues(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}

@RestController
class IssueController(private val gitHubApiClient: GitHubApiClient)
```

## 提供的能力

| 能力 | 实现方式 |
|------|----------|
| 每个接口一个 Bean，无需工厂代码 | `@CoApi` + Spring Boot 自动配置，或 `@EnableCoApi` |
| 响应式或阻塞式 | `WebClient` 或 `RestClient`，由 `coapi.mode` 指定或根据 classpath 推断 |
| 客户端负载均衡 | `serviceId`、`lb://` 或 `@LoadBalanced`，基于 Spring Cloud LoadBalancer |
| 无需代码的单客户端配置 | `coapi.clients.<name>.*` 覆盖 base URL、负载均衡、过滤器和拦截器 |
| 全局扩展点 | `WebClientBuilderCustomizer` / `RestClientBuilderCustomizer` Bean |
| 令牌认证 | `BearerTokenFilter` 配合自动刷新的令牌缓存（响应式） |

## 适用场景

当你通过类型化接口调用多个 HTTP API，并希望每个接口都以最少的配置成为 Bean（无论哪种编程模型）时，CoApi 很合适。尤其是：

- **响应式应用**：Spring Cloud OpenFeign 不支持响应式。CoApi 把 `WebClient` 作为一等选项。
- **共享契约**：服务提供方实现 `@HttpExchange` 接口，消费方用 `@CoApi` 继承它（见[示例](../deep-dive/examples.md)）。
- **服务发现**：客户端指向 `serviceId` 而不是具体主机。

以下场景不适合 CoApi：WebSocket 或 SSE 客户端；弹性策略（重试、熔断，应结合 Resilience4j 等库通过过滤器/拦截器实现）；以及类型化接口带不来收益的一次性调用。这些场景请直接使用 `WebClient` 或 `RestClient`。

## 工作原理

CoApi 为每个客户端接口注册两个 Bean：

1. `<name>.HttpClient`：一个 `WebClient` 或 `RestClient`，配置了客户端的 base URL、过滤器或拦截器，并在需要时启用负载均衡。
2. `<name>.CoApi`：由 `HttpServiceProxyFactory` 基于该客户端创建的接口代理。你注入的就是它。

`<name>` 取 `@CoApi(name)`，默认为接口的简单类名。完整的注册流程见[架构](../deep-dive/architecture.md)。

## 版本兼容性

| CoApi | Spring Boot | Spring Framework | JDK |
|-------|-------------|------------------|-----|
| 3.x | 4.x | 7.x | 17+ |
| 2.x | 4.x | 7.x | 17+ |
| 1.x | 3.2.x | 6.1.x | 17+ |

当前版本线在 JDK 17、21 和 25 上测试。从 2.x 升级？请阅读[迁移到 3.0](./migration-v3.md)。

## 下一步

- [安装](./installation.md)：添加依赖。
- [快速入门](./quick-start.md)：定义并调用第一个客户端。
