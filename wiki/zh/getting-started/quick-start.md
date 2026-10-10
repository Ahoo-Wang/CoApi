---
title: 快速入门
description: 定义 CoApi 客户端接口、配置 base URL 并注入使用，支持响应式（WebClient）和同步（RestClient）两种风格。
---

# 快速入门

本页假设你有一个已[安装依赖](./installation.md)的 Spring Boot 应用。

## 1. 声明客户端

用 Spring 的 `@HttpExchange` 系列注解编写接口，并标注 `@CoApi`。把它放在 `@SpringBootApplication` 类所在的包或其子包中，这样它会被自动发现。

::: code-group

```kotlin [响应式 (Kotlin)]
@CoApi(baseUrl = "\${github.url}")
interface GitHubApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssues(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}

data class Issue(val url: String)
```

```java [同步 (Java)]
@CoApi(baseUrl = "${github.url}")
public interface GitHubApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    List<Issue> getIssues(@PathVariable String owner, @PathVariable String repo);
}

record Issue(String url) {} // 包级可见，两个类型可放在同一文件中
```

:::

在 Kotlin 中，占位符需要写成 `\${...}`，以免被当作字符串模板。

## 2. 配置 base URL

```yaml
github:
  url: https://api.github.com
```

占位符必须能被解析，否则启动会以 `Could not resolve placeholder 'github.url'` 失败。可以用 `${github.url:https://api.github.com}` 提供默认值。

## 3. 注入并调用

::: code-group

```kotlin [响应式 (Kotlin)]
@RestController
class IssueController(private val gitHubApiClient: GitHubApiClient) {
    @GetMapping("/issues")
    fun issues(): Flux<Issue> = gitHubApiClient.getIssues("Ahoo-Wang", "CoApi")
}
```

```java [同步 (Java)]
@RestController
public class IssueController {
    private final GitHubApiClient gitHubApiClient;

    public IssueController(GitHubApiClient gitHubApiClient) {
        this.gitHubApiClient = gitHubApiClient;
    }

    @GetMapping("/issues")
    public List<Issue> issues() {
        return gitHubApiClient.getIssues("Ahoo-Wang", "CoApi");
    }
}
```

:::

完成。CoApi 注册了 `GitHubApiClient.HttpClient` Bean（`WebClient` 或 `RestClient`）和 `GitHubApiClient.CoApi` Bean（你注入的代理）。

## 调用服务发现中的服务

要调用注册在服务发现中的服务，用 `serviceId` 代替 `baseUrl`，并添加 `spring-cloud-starter-loadbalancer`：

```kotlin
@CoApi(serviceId = "order-service")
interface OrderClient {
    @GetExchange("orders/{id}")
    fun getOrder(@PathVariable id: String): Mono<Order>
}
```

见[负载均衡](../deep-dive/load-balancing.md)。

## 接下来

| 我想要…… | 阅读 |
|----------|------|
| 注册其他包或 JAR 中的客户端 | [注册客户端](../deep-dive/auto-configuration.md) |
| 按环境覆盖 URL | [配置参考](./configuration.md) |
| 添加请求头、认证、超时或连接池 | [自定义](../deep-dive/customization.md)、[认证](../deep-dive/authentication.md) |
| 理解启动错误 | [故障排查](./troubleshooting.md) |
