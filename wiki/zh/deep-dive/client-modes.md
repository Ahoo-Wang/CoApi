---
title: 客户端模式
description: CoApi 如何在 WebClient（响应式）和 RestClient（同步）之间选择，以及两种模式对返回类型和依赖的影响。
---

# 客户端模式

CoApi 用 `WebClient` 或 `RestClient` 支撑每个客户端。**客户端模式**决定使用哪一个，并作用于应用上下文中的所有客户端。

| 模式 | HTTP 客户端 | 适配器 | 需要的 Builder Bean |
|------|-------------|--------|---------------------|
| `REACTIVE` | `WebClient` | `WebClientAdapter` | `WebClient.Builder` |
| `SYNC` | `RestClient` | `RestClientAdapter` | `RestClient.Builder` |
| `AUTO`（默认） | 启动时解析为以上之一 | | |

```yaml
coapi:
  mode: SYNC   # REACTIVE | SYNC | AUTO，不区分大小写
```

## `AUTO` 如何判定

`AUTO` 检查 classpath 上是否有 Spring WebFlux（类 `org.springframework.web.reactive.HandlerResult`）：

- 存在：`REACTIVE`
- 不存在：`SYNC`

这是 classpath 检查，而不是 Spring Boot 的 Web 应用类型判断。一个 classpath 上带有 WebFlux 的 Spring MVC 应用（例如为了在别处使用 `WebClient`）会被解析为 `REACTIVE`。如果这不是你想要的，请显式设置模式。显式模式也能让测试结果确定。

## 返回类型

模式决定由哪个 Spring 适配器执行接口方法，因此会限制返回类型：

- **响应式模式**：声明 `Mono<T>`、`Flux<T>` 或 `Mono<ResponseEntity<T>>`。Spring 的 `WebClientAdapter` 也接受阻塞式返回类型（`T`、`List<T>`），但会阻塞调用线程，不要在事件循环线程上使用。
- **同步模式**：声明普通类型：`T`、`List<T>`、`ResponseEntity<T>`、`Unit`/`void`。`RestClientAdapter` 不能返回 `Mono`/`Flux`。

如果将来可能切换模式，请保持返回类型对两种模式都兼容，或使用不同的接口。

## Builder Bean

两种模式下，CoApi 都会从容器中获取**一个** Builder Bean，为客户端进行配置，然后调用 `build()`：

- Spring Boot 通过 `spring-boot-starter-webclient` / `spring-boot-starter-restclient` 提供 prototype 作用域的 Builder。Boot 自身对 Builder 的定制（编解码器、消息转换器、可观测性）因此同样作用于 CoApi 客户端。
- 存在多个 Builder Bean 时，把其中一个标记为 `@Primary`。
- 自己定义 Builder 时，请使用 `@Scope("prototype")`。每个客户端都会修改它拿到的 Builder。

CoApi 对 Builder 做了什么、按什么顺序，见[自定义](./customization.md)。

## 混用模式

每个应用上下文只有一种模式。如果同一应用中有的 API 要响应式调用、有的要同步调用，选择 `REACTIVE`：如上所述，阻塞式返回类型在 `WebClient` 适配器上仍然可用。另一种做法是对个别 API 单独使用 `WebClient` 或 `RestClient`。
