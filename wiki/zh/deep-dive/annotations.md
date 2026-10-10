---
title: 定义客户端
description: '@CoApi 与 @LoadBalanced 注解、base URL 解析、客户端命名，以及共享契约和动态 URI 的用法。'
---

# 定义客户端

CoApi 客户端是一个带有 Spring `@HttpExchange` 方法、并标注了 `@CoApi` 的**接口**。请求本身（路径、方法、请求头、请求体）完全使用标准的 Spring [HTTP Interface](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface)。CoApi 只负责请求发往哪里，以及客户端如何装配。

## `@CoApi`

| 属性 | 默认值 | 含义 |
|------|--------|------|
| `baseUrl` | `""` | 目标的 base URL，如 `https://api.github.com`、`${github.url}` 或 `lb://order-service`。占位符在启动时解析，且必须存在。 |
| `serviceId` | `""` | 服务发现 ID。仅在 `baseUrl` 为空时使用，等价于 `lb://<serviceId>`。支持占位符。 |
| `name` | 接口简单类名 | 客户端名称：用于 Bean 名，也作为 `coapi.clients.<name>` 配置键。必须唯一。 |

```kotlin
@CoApi(baseUrl = "\${github.url}")            // 来自配置的固定地址
interface GitHubApiClient

@CoApi(serviceId = "order-service")           // 服务发现，负载均衡
interface OrderClient

@CoApi(baseUrl = "lb://order-service")        // 与 serviceId 等价
interface OrderClientViaUrl

@CoApi(name = "GitHubApi", serviceId = "github-service")  // 自定义名称
interface ServiceApiClient
```

`baseUrl` 和 `serviceId` 只设置其一。两者都设置时，`baseUrl` 优先。

## Base URL 解析

1. 使用非空的 `baseUrl`，并解析占位符。
2. 否则，非空的 `serviceId` 转为 `lb://<serviceId>`。
3. 否则 base URL 为空。

随后，如果 URL 以 `lb://` 开头（不区分大小写），CoApi 会把它改写为 `http://` 并把客户端标记为**负载均衡**。之后由 Spring Cloud LoadBalancer 把主机名替换为真实实例。见[负载均衡](./load-balancing.md)。

`coapi.clients.<name>.base-url` 和 `.load-balanced` 可以按环境覆盖以上所有内容。见[覆盖规则](../getting-started/configuration.md#覆盖规则)。

## `@LoadBalanced`

`me.ahoo.coapi.api.LoadBalanced`（不是 Spring Cloud 的同名注解）在保留普通 URL 的同时把客户端标记为负载均衡：

```kotlin
@CoApi(baseUrl = "http://order-service")
@LoadBalanced
interface OrderClient
```

它与 `@CoApi(serviceId = "order-service")` 等价。当 URL 来自不应包含 `lb://` 的占位符时很有用。

## 客户端名称与 Bean

每个客户端会得到两个以其名称命名的 Bean：

| Bean | 类型 | 用途 |
|------|------|------|
| `<name>.HttpClient` | `WebClient` 或 `RestClient` | 底层 HTTP 客户端 |
| `<name>.CoApi` | 你的接口 | 你注入的代理 |

按类型注入接口即可。只有在需要直接使用同一目标的原始客户端时，才按名称获取 `WebClient`/`RestClient` Bean。

两个客户端名称相同会导致启动失败（`Duplicate CoApi name`）。当不同包中的接口简单类名相同时就会发生，给其中一个设置 `name` 即可。

## 没有 base URL

没有 base URL 的客户端仍然可用：每次请求时通过 `URI` 或 `UriBuilderFactory` 参数传入目标地址，Spring HTTP Interface 原生支持这种方式。

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

也可以不在代码里写 URL，而是通过 `coapi.clients.UriApiClient.base-url` 提供。

## 共享契约

服务提供方和消费方可以共享同一个接口，使服务端实现与客户端不会出现偏差：

```kotlin
// provider-api 模块：普通的 Spring HTTP Interface
@HttpExchange("todo")
interface TodoApi {
    @GetExchange
    fun getTodo(): Flux<Todo>
}

// provider-api 模块：客户端，只依赖 coapi-api
@CoApi(serviceId = "provider-service")
interface TodoClient : TodoApi

// 服务提供方：实现契约
@RestController
class TodoController : TodoApi {
    override fun getTodo(): Flux<Todo> = Flux.range(1, 10).map { Todo("todo-$it") }
}
```

消费方依赖 API 模块并注入 `TodoClient`。由于该客户端位于服务提供方的包中，消费方需要显式注册它：使用 `@EnableCoApi(clients = [TodoClient::class])` 或 `coapi.base-packages`。见[注册客户端](./auto-configuration.md)。
