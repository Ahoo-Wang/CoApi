---
title: 配置参考
description: 全部 coapi.* 属性，以及单客户端属性如何覆盖 @CoApi 注解声明的规则。
---

# 配置参考

所有属性都位于 `coapi` 前缀下，由 Spring Boot starter 绑定（[`CoApiProperties`](https://github.com/Ahoo-Wang/CoApi/blob/main/spring-boot-starter/src/main/kotlin/me/ahoo/coapi/spring/boot/starter/CoApiProperties.kt)）。

## 全局属性

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `coapi.enabled` | `true` | 设为 `false` 会关闭 Spring Boot 自动配置（classpath 扫描和 `coapi.*` 绑定）。`@EnableCoApi` 中列出的客户端仍会注册。 |
| `coapi.mode` | `AUTO` | `REACTIVE`（`WebClient`）、`SYNC`（`RestClient`）或 `AUTO`（根据 classpath 推断）。不区分大小写；非法值会导致启动失败。见[客户端模式](../deep-dive/client-modes.md)。 |
| `coapi.base-packages` | 空 | 在 `@SpringBootApplication` 所在包之外，额外扫描 `@CoApi` 接口的包。支持逗号分隔的字符串或 YAML 列表。 |

## 单客户端属性

`<name>` 是客户端名称：设置了 `@CoApi(name)` 时取该值，否则取接口的简单类名（如 `GitHubApiClient`）。重命名客户端会改变它的配置键。

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `coapi.clients.<name>.base-url` | 未设置 | 替换注解中的 base URL。`lb://` URL 会启用负载均衡。 |
| `coapi.clients.<name>.load-balanced` | 未设置 | `true` 或 `false`，强制开启或关闭负载均衡。 |
| `coapi.clients.<name>.reactive.filter.names` | 空 | 要添加的 `ExchangeFilterFunction` 的 Bean 名称（响应式模式）。 |
| `coapi.clients.<name>.reactive.filter.types` | 空 | 要添加的 `ExchangeFilterFunction` 的 Bean 类型（全限定类名）。 |
| `coapi.clients.<name>.sync.interceptor.names` | 空 | 要添加的 `ClientHttpRequestInterceptor` 的 Bean 名称（同步模式）。 |
| `coapi.clients.<name>.sync.interceptor.types` | 空 | 要添加的 `ClientHttpRequestInterceptor` 的 Bean 类型。 |

过滤器和拦截器必须是 Bean。它们先按名称、再按类型应用，各自保持配置顺序。按类型引用时必须恰好匹配一个 Bean。只有当前模式对应的列表会生效：响应式模式用 `reactive.*`，同步模式用 `sync.*`。

## 覆盖规则

注解定义客户端，单客户端属性在构建 HTTP 客户端时覆盖它。得到的结果称为客户端的**生效定义**（effective definition），[Builder 定制器](../deep-dive/customization.md)接收的也是它。

**Base URL**

1. `coapi.clients.<name>.base-url`（非空时）。
2. 否则取 `@CoApi(baseUrl)`，并解析占位符。
3. 否则取 `lb://` + `@CoApi(serviceId)`。
4. 否则为空。此时客户端必须在每次请求时传入完整的 `URI`（见[定义客户端](../deep-dive/annotations.md#没有-base-url)）。

`lb://` 协议（不区分大小写）总会被改写为 `http://`，并把客户端标记为负载均衡。

**负载均衡**

| `load-balanced` | `base-url` | 是否负载均衡 |
|-----------------|------------|--------------|
| `true` / `false` | 任意 | 取配置值 |
| 未设置 | 已设置 | 仅当它是 `lb://` URL 时 |
| 未设置 | 未设置 | 取决于注解：`serviceId`、`lb://` 形式的 `baseUrl`，或 `@LoadBalanced` |

这些规则集中实现在 `CoApiDefinition.withOverrides()` 中，并由 `ClientPropertiesTest` 覆盖测试。

## 示例

```yaml
coapi:
  mode: SYNC
  base-packages:
    - com.example.partner.clients
  clients:
    # 在当前环境中让负载均衡客户端指向固定主机
    OrderClient:
      base-url: http://orders.staging.internal:8080
    # 为声明为纯 @CoApi 的客户端提供 URL
    PaymentClient:
      base-url: https://payments.example.com
      sync:
        interceptor:
          names:
            - paymentAuthInterceptor
```

## 不使用 Spring Boot

`coapi.clients.*` 属性是 Spring Boot 的功能。在纯 Spring 中，同样的设置由你实现的可选 Bean 提供：

| Bean | 提供 |
|------|------|
| `ClientProperties` | `getBaseUri(name)`、`getLoadBalanced(name)` |
| `ReactiveClientProperties` | `getFilter(name)` |
| `SyncClientProperties` | `getInterceptor(name)` |

它们都是可选的：缺失时，客户端完全按注解声明工作。使用 Spring Boot 时，`CoApiProperties` 同时实现了这三个接口。两种情况下 `coapi.mode` 都从 `Environment` 读取。
