---
title: 故障排查
description: CoApi 启动错误和运行时意外行为的原因与解决方法。
---

# 故障排查

CoApi 在启动时检查配置，出错时给出包含客户端名称和解决方法的消息。在下面找到你遇到的消息。

## 启动错误

### `Invalid coapi.mode value: [...]`

`coapi.mode` 必须是 `REACTIVE`、`SYNC` 或 `AUTO`（不区分大小写）。修正该值或删除该属性。

### `Duplicate CoApi name [...]`

两个客户端解析出了相同的名称：要么 `@CoApi(name)` 相同，要么是不同包中简单类名相同的接口。名称决定 Bean 名和 `coapi.clients.<name>` 配置键，因此必须唯一。给其中一个设置不同的 `@CoApi(name = "...")`。

### `Could not resolve placeholder '...'`

`@CoApi(baseUrl)`、`@CoApi(serviceId)` 或 `CoApiDefinition` Bean 的 `baseUrl` 中的 `${...}` 占位符在当前环境中未定义。定义该属性，或添加默认值：`${github.url:https://api.github.com}`。

### `CoApi [...] is load balanced, but Spring Cloud LoadBalancer is not on the classpath`

客户端使用了 `serviceId`、`lb://` URL 或 `@LoadBalanced`，或者设置了 `coapi.clients.<name>.load-balanced=true`。添加 `org.springframework.cloud:spring-cloud-starter-loadbalancer`。如果当前环境应调用固定主机，把 `coapi.clients.<name>.base-url` 设为普通 URL，或设置 `coapi.clients.<name>.load-balanced=false`。

### `CoApiDefinition bean [...] is declared by a non-static @Bean method`

`CoApiDefinition` Bean 在常规 Bean 后处理之前就会被读取。把 `@Bean` 方法声明为 `static`（Kotlin：放在 `companion object` 中并加 `@JvmStatic`）。见[注册客户端](../deep-dive/auto-configuration.md#coapidefinition-bean)。

### `NoSuchBeanDefinitionException: ... WebClient$Builder` 或 `RestClient$Builder`

缺少当前客户端模式所需的 Builder。添加 `spring-boot-starter-webclient`（响应式）或 `spring-boot-starter-restclient`（同步），或把 `coapi.mode` 设为已有 Builder 对应的模式。见[安装](./installation.md#http-客户端-builder)。

### `NoUniqueBeanDefinitionException: ... WebClient$Builder` 或 `RestClient$Builder`

应用定义了多个 Builder，例如 Boot 的 Builder 之外还有一个 Spring Cloud `@LoadBalanced` Builder。把 CoApi 应使用的那个标记为 `@Primary`。

### `JWT token has no exp claim`

`jwtToExpirableToken()` 需要带 `exp` 声明的 JWT。对于没有该声明的令牌，用显式过期时间构造 `ExpirableToken(token, expireAt)`。

## 找不到客户端

客户端接口报 `NoSuchBeanDefinitionException`，说明它从未被注册。请检查：

1. 它是标注了 `@CoApi` 的**接口**。类会被忽略。
2. 它位于 `@SpringBootApplication` 所在包或其子包中、位于 `coapi.base-packages` 列出的包中，或已列在 `@EnableCoApi(clients = [...])` 中。
3. 如果依赖扫描注册（未使用 `@EnableCoApi`），确认 `coapi.enabled` 没有被设为 `false`。该设置会关闭扫描，但不影响 `@EnableCoApi` 中列出的客户端。

## 运行时意外

### 客户端用的是 `WebClient`，而我期望 `RestClient`（或反之）

在 `coapi.mode=AUTO` 下，只要 classpath 上有 Spring WebFlux 就会选择响应式模式，即使是 Servlet 应用。请显式设置 `coapi.mode`。该模式作用于应用中的所有客户端。见[客户端模式](../deep-dive/client-modes.md)。

### 过滤器或拦截器被应用两次，或在客户端之间串用

CoApi 会修改它从容器中拿到的 Builder Bean。Spring Boot 的 Builder 是 prototype 作用域，因此每个客户端拿到的都是新实例。如果你自己声明了 `WebClient.Builder` 或 `RestClient.Builder` Bean，也请标注 `@Scope("prototype")`，否则所有客户端会修改同一个 Builder。

### `serviceId` 客户端因没有可用实例而失败

负载均衡本身在工作，但服务发现中没有该 `serviceId` 的实例。检查服务发现注册情况。静态配置时，在 `spring.cloud.discovery.client.simple.instances.<serviceId>` 下列出实例。

### 单客户端属性不生效

`coapi.clients` 下的键必须与客户端名称完全一致。如果接口设置了 `@CoApi(name = "GitHubApi")`，键就是 `coapi.clients.GitHubApi`，而不是接口名。另外请确认配置的是当前模式对应的列表：`reactive.filter` 或 `sync.interceptor`。

## 调试日志

CoApi 会以 `INFO` 级别记录每个注册的 Bean（`Register WebClient [GitHubApiClient.HttpClient].`）。当同名 Bean 已存在而跳过注册时，会以 `WARN` 级别记录。需要更多细节时：

```yaml
logging:
  level:
    me.ahoo.coapi: DEBUG
```
