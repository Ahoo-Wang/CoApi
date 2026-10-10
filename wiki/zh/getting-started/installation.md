---
title: 安装
description: 添加 CoApi starter、与客户端模式匹配的 HTTP 客户端 starter，以及可选的负载均衡和 JWT 支持。
---

# 安装

一个 CoApi 应用的 classpath 上需要三样东西：

1. **CoApi starter**。
2. **HTTP 客户端 Builder**，与[客户端模式](../deep-dive/client-modes.md)匹配：`WebClient`（响应式）或 `RestClient`（同步）。
3. **可选依赖**：`serviceId`/`lb://` 客户端需要 Spring Cloud LoadBalancer，解析 JWT 需要 `java-jwt`。

将 `<version>` 替换为[最新版本](https://github.com/Ahoo-Wang/CoApi/releases/latest)（见[版本兼容性](./overview.md#版本兼容性)）。

## CoApi starter

::: code-group

```kotlin [Gradle (Kotlin)]
implementation("me.ahoo.coapi:coapi-spring-boot-starter:<version>")
```

```groovy [Gradle (Groovy)]
implementation 'me.ahoo.coapi:coapi-spring-boot-starter:<version>'
```

```xml [Maven]
<dependency>
    <groupId>me.ahoo.coapi</groupId>
    <artifactId>coapi-spring-boot-starter</artifactId>
    <version><version></version>
</dependency>
```

:::

如需用一个版本号管理所有 CoApi 构件，可导入 BOM `me.ahoo.coapi:coapi-bom`，然后省略各依赖的版本：

::: code-group

```kotlin [Gradle (Kotlin)]
implementation(platform("me.ahoo.coapi:coapi-bom:<version>"))
implementation("me.ahoo.coapi:coapi-spring-boot-starter")
```

```xml [Maven]
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>me.ahoo.coapi</groupId>
            <artifactId>coapi-bom</artifactId>
            <version><version></version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

:::

## HTTP 客户端 Builder

CoApi 基于应用中的 `WebClient.Builder` 或 `RestClient.Builder` Bean 构建每个客户端，而 CoApi starter **不会**引入它们。在 Spring Boot 4 中，它们来自独立的 starter：

| 客户端模式 | 添加依赖 |
|------------|----------|
| 响应式（`WebClient`） | `org.springframework.boot:spring-boot-starter-webclient` |
| 同步（`RestClient`） | `org.springframework.boot:spring-boot-starter-restclient` |

缺少对应的 Builder 时，启动会因 `WebClient.Builder` 或 `RestClient.Builder` 的 `NoSuchBeanDefinitionException` 失败。

::: warning AUTO 模式取决于 classpath
在默认的 `coapi.mode=AUTO` 下，只要 classpath 上有 Spring WebFlux，CoApi 就会使用 `WebClient`，即使是 Servlet 应用也一样。如果在存在 WebFlux 的情况下需要 `RestClient`，请设置 `coapi.mode=SYNC`。见[客户端模式](../deep-dive/client-modes.md)。
:::

## 可选依赖

**负载均衡**（`serviceId`、`lb://`、`@LoadBalanced`）需要 Spring Cloud LoadBalancer，以及与 Spring Boot 版本匹配的 Spring Cloud BOM：

```kotlin
implementation(platform("org.springframework.cloud:spring-cloud-dependencies:<spring-cloud-version>"))
implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
```

**JWT 过期时间解析**（`String.jwtToExpirableToken()`，见[认证](../deep-dive/authentication.md)）需要 `com.auth0:java-jwt`：

```kotlin
implementation("com.auth0:java-jwt:<version>")
```

## 构件

| 构件 | 内容 | 适用情况 |
|------|------|----------|
| `coapi-spring-boot-starter` | 自动配置与 `coapi.*` 属性 | Spring Boot 应用（最常见） |
| `coapi-spring` | 注册器、FactoryBean、客户端 SPI、认证过滤器 | 不使用 Boot 的纯 Spring，配合 `@EnableCoApi` |
| `coapi-api` | 仅 `@CoApi` 和 `@LoadBalanced` | 声明客户端但不应依赖 Spring Boot 的 API 模块 |
| `coapi-bom` | 上述构件的版本对齐 | 统一导入版本 |

拆分出 `coapi-api` 后，服务提供方可以发布一个轻量的客户端模块（如 [`example-provider-api`](https://github.com/Ahoo-Wang/CoApi/tree/main/example/example-provider-api)），消费方引入它时不会连带引入 Spring Boot。

## 下一步

[快速入门](./quick-start.md)：定义并调用第一个客户端。
