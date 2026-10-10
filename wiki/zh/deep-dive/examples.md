---
title: 示例
description: CoApi 仓库中可运行的示例应用，以及每个示例展示的用法。
---

# 示例

[`example/`](https://github.com/Ahoo-Wang/CoApi/tree/main/example) 目录包含可运行的应用。它们涵盖客户端定义、注册、客户端模式、负载均衡和自定义，但不包含认证，认证示例见[认证](./authentication.md)。

| 模块 | 展示内容 |
|------|----------|
| [`example-provider-api`](https://github.com/Ahoo-Wang/CoApi/tree/main/example/example-provider-api) | 共享契约：`TodoApi`（`@HttpExchange`）和 `TodoClient : TodoApi`（`@CoApi(serviceId = "provider-service")`），只依赖 `coapi-api` |
| [`example-provider-server`](https://github.com/Ahoo-Wang/CoApi/tree/main/example/example-provider-server) | 在 `@RestController` 中实现 `TodoApi` 的服务提供方（端口 8010） |
| [`example-consumer-client`](https://github.com/Ahoo-Wang/CoApi/tree/main/example/example-consumer-client) | 客户端库：占位符 `baseUrl`、带自定义 `name` 的 `serviceId`、单客户端过滤器、接收 `URI`/`UriBuilderFactory` 的无 base URL 客户端 |
| [`example-consumer-server`](https://github.com/Ahoo-Wang/CoApi/tree/main/example/example-consumer-server) | 响应式消费方：`@EnableCoApi` 加 `coapi.base-packages`、静态服务发现实例、按 Bean 名称和类型配置的过滤器、按客户端划分的连接池定制器 |
| [`example-sync`](https://github.com/Ahoo-Wang/CoApi/tree/main/example/example-sync) | 基于 `RestClient` 的 Java 同步客户端（返回 `List<Issue>`），包括负载均衡和非负载均衡两种 |

## 运行

测试会让各应用连接本地替身服务，因此不需要网络：

```bash
./gradlew :example-consumer-server:test :example-provider-server:test :example-sync:test
```

要手动体验提供方/消费方组合，在 IDE 中先启动 `ProviderServer`（端口 8010），再启动 `ConsumerServer`（端口 8080），然后请求 `GET http://localhost:8080/todo`。消费方通过其 `application.yaml` 中的 simple discovery 实例解析 `provider-service`。

## 各用法的说明页面

| 用法 | 页面 |
|------|------|
| 提供方/消费方共享契约 | [定义客户端](./annotations.md#共享契约) |
| 来自其他模块的客户端 | [注册客户端](./auto-configuration.md) |
| 按名称/类型配置过滤器、连接池定制器 | [自定义](./customization.md) |
| 基于静态实例的 `serviceId` | [负载均衡](./load-balancing.md) |
| Java 同步客户端 | [客户端模式](./client-modes.md) |
