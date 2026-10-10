# CoApi

> [中文文档](https://coapi.ahoo.me/zh/) | [Documentation](https://coapi.ahoo.me/) | [English README](./README.md)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://github.com/Ahoo-Wang/CoApi/blob/main/LICENSE)
[![GitHub release](https://img.shields.io/github/release/Ahoo-Wang/CoApi.svg)](https://github.com/Ahoo-Wang/CoApi/releases)
[![Maven Central Version](https://img.shields.io/maven-central/v/me.ahoo.coapi/coapi-api)](https://central.sonatype.com/artifact/me.ahoo.coapi/coapi-api)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/709bea2aec1d4cfd85991edf66b5ccbc)](https://app.codacy.com/gh/Ahoo-Wang/CoApi/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Codecov](https://codecov.io/gh/Ahoo-Wang/CoApi/graph/badge.svg?token=ayVd7lthB6)](https://codecov.io/gh/Ahoo-Wang/CoApi)
[![Integration Test Status](https://github.com/Ahoo-Wang/CoApi/actions/workflows/integration-test.yml/badge.svg)](https://github.com/Ahoo-Wang/CoApi)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Ahoo-Wang/CoApi)

**零样板代码的 Spring HTTP Interface 客户端，支持响应式与同步。**

Spring 的 [HTTP Interface](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface) 允许用 `@HttpExchange` 接口声明 HTTP API，但你仍需为每个接口构建客户端、适配器和代理并注册 Bean。CoApi 替你完成这些：给接口标注 `@CoApi`，然后直接注入。

- **响应式或同步**：底层使用 `WebClient` 或 `RestClient`，由 `coapi.mode` 指定或根据 classpath 推断。
- **客户端负载均衡**：通过 Spring Cloud LoadBalancer 支持 `serviceId`、`lb://` 或 `@LoadBalanced`。
- **单客户端配置**：用 `coapi.clients.<name>.*` 覆盖 base URL、负载均衡、过滤器和拦截器。
- **可扩展**：Builder 定制器 Bean、可替换的 Bean，以及可自动刷新的 Bearer 令牌过滤器。

## 兼容性

| CoApi | Spring Boot | Spring Framework | JDK |
|-------|-------------|------------------|-----|
| 3.x | 4.x | 7.x | 17+ |
| 2.x | 4.x | 7.x | 17+ |
| 1.x | 3.2.x | 6.1.x | 17+ |

从 2.x 升级：[迁移到 3.0](https://coapi.ahoo.me/zh/getting-started/migration-v3)。

## 安装

```kotlin
implementation("me.ahoo.coapi:coapi-spring-boot-starter:<version>")
// 以及与客户端模式匹配的 Builder（Spring Boot 4）：
implementation("org.springframework.boot:spring-boot-starter-webclient")   // 响应式
// implementation("org.springframework.boot:spring-boot-starter-restclient") // 同步
// 可选，用于 serviceId / lb:// 客户端：
// implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
```

Maven、BOM 及可选依赖见[安装](https://coapi.ahoo.me/zh/getting-started/installation)。

## 使用

在 `@SpringBootApplication` 所在包或其子包中声明客户端：

```kotlin
@CoApi(baseUrl = "\${github.url}")
interface GitHubApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssues(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}
```

```yaml
github:
  url: https://api.github.com
```

注入并调用：

```kotlin
@RestController
class IssueController(private val gitHubApiClient: GitHubApiClient) {
    @GetMapping("/issues")
    fun issues(): Flux<Issue> = gitHubApiClient.getIssues("Ahoo-Wang", "CoApi")
}
```

要调用服务发现中的服务，改用 `@CoApi(serviceId = "order-service")`。同步客户端使用 `List<Issue>` 等普通返回类型。

## 文档

| | |
|-|-|
| [快速入门](https://coapi.ahoo.me/zh/getting-started/quick-start) | 逐步创建响应式和同步客户端 |
| [配置参考](https://coapi.ahoo.me/zh/getting-started/configuration) | 全部 `coapi.*` 属性与覆盖规则 |
| [负载均衡](https://coapi.ahoo.me/zh/deep-dive/load-balancing) | `serviceId`、`lb://`、按环境覆盖 |
| [自定义](https://coapi.ahoo.me/zh/deep-dive/customization) | 过滤器、拦截器、Builder 定制器 |
| [故障排查](https://coapi.ahoo.me/zh/getting-started/troubleshooting) | 启动错误及解决方法 |
| [示例](./example) | 可运行的提供方/消费方及同步应用 |

## 参与贡献

见 [CONTRIBUTING.md](./CONTRIBUTING.md)。安全问题见 [SECURITY.md](./SECURITY.md)。

## 许可证

[Apache License 2.0](./LICENSE)
