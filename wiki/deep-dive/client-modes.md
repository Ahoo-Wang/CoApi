---
title: Client Modes
description: Deep dive into CoApi client modes for reactive and synchronous HTTP requests
---

# 客户端模式

CoApi 提供了灵活的客户端模式，以支持不同的编程范式和性能需求。框架会根据类路径自动检测合适的模式，也支持显式配置。

## 概述

ClientMode 决定了 CoApi 应用中 HTTP 请求的执行和管理方式。框架支持三种模式：

- **REACTIVE（响应式）**：基于 WebClient 的异步、非阻塞 HTTP 请求
- **SYNC（同步）**：基于 RestClient 的同步、阻塞 HTTP 请求
- **AUTO（自动）**：基于类路径检测的智能模式推断

AUTO 模式是默认行为，它会适应可用的 Spring Web 依赖，非常适合需要在不同环境中工作而无需手动配置的应用程序。

## 一览

| 模式 | HTTP 客户端 | 类型 | 依赖 | 性能 |
|------|-------------|------|------|------|
| REACTIVE | WebClient | 异步 | spring-boot-webclient | 高吞吐量 |
| SYNC | RestClient | 同步 | spring-boot-web | 低延迟 |
| AUTO | WebClient/RestClient | 混合 | 两者 | 视情况而定 |

## 模式检测逻辑

AUTO 模式使用智能检测来根据可用依赖确定合适的客户端类型：

```mermaid
flowchart TD
    A[Start Mode Detection] --> B{"Check for org.springframework.web.reactive.HandlerResult"}
    B -->|Present| C[REACTIVE Mode]
    B -->|Absent| D[SYNC Mode]
    C --> E[Create WebClient-based Adapter]
    D --> F[Create RestClient-based Adapter]
    E --> G[End Detection]
    F --> G
    G --> H[Apply coapi.mode Override if Present]
```

### 检测流程

AUTO 模式检测在 [ClientMode.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/ClientMode.kt) 中实现：

```kotlin
enum class ClientMode {
    REACTIVE, SYNC, AUTO;

    companion object {
        fun detect(): ClientMode {
            return try {
                Class.forName("org.springframework.web.reactive.HandlerResult")
                REACTIVE
            } catch (e: ClassNotFoundException) {
                SYNC
            }
        }
    }
}
```

## 架构概述

客户端模式架构遵循 SPI（服务提供者接口）模式，包含工厂适配器：

```mermaid
classDiagram
    class HttpExchangeAdapterFactory {
        <<interface>>
        create(beanFactory, httpClientName)
    }

    class ReactiveHttpExchangeAdapterFactory {
        create(beanFactory, httpClientName)
    }

    class SyncHttpExchangeAdapterFactory {
        create(beanFactory, httpClientName)
    }

    class HttpClientConfig {
        baseUrl: String
        timeout: Duration
        retries: Int
    }

    HttpExchangeAdapterFactory <|.. ReactiveHttpExchangeAdapterFactory
    HttpExchangeAdapterFactory <|.. SyncHttpExchangeAdapterFactory
    ReactiveHttpExchangeAdapterFactory --> HttpClientConfig
    SyncHttpExchangeAdapterFactory --> HttpClientConfig
```

HttpExchangeAdapterFactory 接口提供了一种统一的方式来创建 HTTP 交换适配器，无论底层客户端实现如何：

```kotlin
// spring/src/main/kotlin/me/ahoo/coapi/spring/HttpExchangeAdapterFactory.kt
interface HttpExchangeAdapterFactory {
    fun create(beanFactory: BeanFactory, httpClientName: String): HttpExchangeAdapter
}
```

## 响应式堆栈实现

响应式堆栈使用 WebClient 进行非阻塞 HTTP 请求，与响应式编程范式全面集成。

```mermaid
sequenceDiagram
    autonumber
    participant A as Application
    participant B as AbstractWebClientFactoryBean
    participant C as WebClient.Builder
    participant D as ExchangeFilterFunctions
    participant E as LoadBalancedExchangeFilterFunction
    participant F as WebClientFactoryBean
    participant G as WebClientAdapter
    participant H as HttpExchangeAdapter

    A->>B: createWebClient()
    B->>C: WebClient.Builder.fromApplicationContext()
    C->>B: baseUrl()
    B->>D: applyFilterFunctions()
    D->>B: configure()
    B->>F: create()
    F->>E: applyLoadBalancer()
    E->>F: configure()
    F->>G: create(webClient)
    G->>H: create()
    H->>A: return HttpExchangeAdapter
```

### FactoryBean 层次结构

响应式堆栈遵循以下继承层次结构：

- **AbstractWebClientFactoryBean**：配置 WebClient.Builder 的基类
  - 从 Spring 应用上下文获取 WebClient.Builder
  - 应用 ClientProperties 中的 ExchangeFilterFunctions
  - 应用 WebClientBuilderCustomizer beans
  - 设置 baseUrl 和超时

- **WebClientFactoryBean**：扩展负载均衡器支持
  - 当负载均衡器可用时添加 LoadBalancedExchangeFilterFunction
  - 配置响应式重试机制
  - 启用断路器集成

- **WebClientAdapter**：将 WebClient 转换为 HttpExchangeAdapter
  - 实现 HttpExchangeAdapter 接口
  - 处理请求/响应映射
  - 支持响应式流集成

```kotlin
// spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/AbstractWebClientFactoryBean.kt
abstract class AbstractWebClientFactoryBean : FactoryBean<WebClient> {
    override fun getObject(): WebClient {
        val builder = webClientBuilder()
            .baseUrl(clientProperties.baseUrl)

        clientProperties.filterFunctions.forEach { filterFunction ->
            builder.filter(filterFunction)
        }

        return builder.build()
    }
}
```

## 同步堆栈实现

同步堆栈使用 RestClient 提供传统的同步 HTTP 请求，配置简单，对于常见用例具有出色的性能。

```mermaid
sequenceDiagram
    autonumber
    participant A as Application
    participant B as AbstractRestClientFactoryBean
    participant C as RestClient.Builder
    participant D as ClientHttpRequestInterceptors
    participant E as LoadBalancerInterceptor
    participant F as RestClientFactoryBean
    participant G as RestClientAdapter
    participant H as HttpExchangeAdapter

    A->>B: createRestClient()
    B->>C: RestClient.Builder.fromApplicationContext()
    C->>B: baseUrl()
    B->>D: applyInterceptors()
    D->>B: configure()
    B->>F: create()
    F->>E: applyLoadBalancer()
    E->>F: configure()
    F->>G: create(restClient)
    G->>H: create()
    H->>A: return HttpExchangeAdapter
```

### FactoryBean 层次结构

同步堆栈遵循以下继承层次结构：

- **AbstractRestClientFactoryBean**：配置 RestClient.Builder 的基类
  - 从 Spring 应用上下文获取 RestClient.Builder
  - 应用 ClientProperties 中的 ClientHttpRequestInterceptors
  - 应用 RestClientBuilderCustomizer beans
  - 设置 baseUrl 和超时

- **RestClientFactoryBean**：扩展负载均衡器支持
  - 当负载均衡器可用时添加 LoadBalancerInterceptor
  - 配置重试机制
  - 启用连接池优化

- **RestClientAdapter**：将 RestClient 转换为 HttpExchangeAdapter
  - 实现 HttpExchangeAdapter 接口
  - 处理请求/响应映射
  - 提供阻塞 I/O 操作

```kotlin
// spring/src/main/kotlin/me/ahoo/coapi/spring/client/sync/AbstractRestClientFactoryBean.kt
abstract class AbstractRestClientFactoryBean : FactoryBean<RestClient> {
    override fun getObject(): RestClient {
        val builder = restClientBuilder()
            .baseUrl(clientProperties.baseUrl)

        clientProperties.interceptors.forEach { interceptor ->
            builder.requestInterceptor(interceptor)
        }

        return builder.build()
    }
}
```

## 适配器创建

响应式和同步堆栈都实现各自的工厂适配器：

```mermaid
sequenceDiagram
    autonumber
    participant ClientMode
    participant FactoryBean
    participant HttpClientBuilder
    participant LoadBalancer
    participant HttpClientAdapter

    ClientMode->>FactoryBean: create(beanFactory, httpClientName)
    FactoryBean->>HttpClientBuilder: configure()
    HttpClientBuilder->>LoadBalancer: apply()
    FactoryBean->>HttpClientAdapter: create(httpClient)
    HttpClientAdapter->>ClientMode: return HttpExchangeAdapter
```

工厂适配器创建相应的适配器：

```kotlin
// spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/ReactiveHttpExchangeAdapterFactory.kt
class ReactiveHttpExchangeAdapterFactory : HttpExchangeAdapterFactory {
    override fun create(beanFactory: BeanFactory, httpClientName: String): HttpExchangeAdapter {
        val webClient = WebClientFactoryBean().create(beanFactory, httpClientName)
        return WebClientAdapter.create(webClient)
    }
}

// spring/src/main/kotlin/me/ahoo/coapi/spring/client/sync/SyncHttpExchangeAdapterFactory.kt
class SyncHttpExchangeAdapterFactory : HttpExchangeAdapterFactory {
    override fun create(beanFactory: BeanFactory, httpClientName: String): HttpExchangeAdapter {
        val restClient = RestClientFactoryBean().create(beanFactory, httpClientName)
        return RestClientAdapter.create(restClient)
    }
}
```

## 配置属性

CoApi 通过属性支持全面配置：

```properties
# Client mode configuration (auto, reactive, sync)
coapi.mode=auto

# Base URL for all HTTP requests
coapi.client.base-url=https://api.example.com

# Timeout configuration
coapi.client.timeout=30s
coapi.client.read-timeout=60s
coapi.client.connect-timeout=10s

# Retry configuration
coapi.client.retries=3
coapi.client.retry-backoff=exponential
coapi.client.retry-backoff-initial=100ms
coapi.client.retry-backoff-max=2s

# Load balancer configuration
coapi.client.load-balancer.enabled=true
coapi.client.load-balancer.strategy=round-robin
```

## 特性变体

Gradle 特性变体支持选择性依赖包含：

```kotlin
// spring/build.gradle.kts
features {
    reactiveSupport {
        usingSourceSet(sourceSets.getByName("main"))
        api("org.springframework.boot:spring-boot-starter-webflux")
    }
    lbSupport {
        usingSourceSet(sourceSets.getByName("main"))
        api("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    }
    jwtSupport {
        usingSourceSet(sourceSets.getByName("main"))
        api("io.jsonwebtoken:jjwt-api:0.11.5")
    }
}
```

### 可用特性

- **reactiveSupport**：使用 `spring-boot-webclient` 启用基于 WebClient 的响应式客户端
- **lbSupport**：使用 `spring-cloud-commons` 添加负载均衡器支持
- **jwtSupport**：使用 `java.jwt` 包含 JWT 认证支持

## 性能特性

### 响应式模式优势

- **高吞吐量**：非阻塞 I/O 可处理数千个并发连接
- **资源效率**：高负载下线程使用最少
- **背压支持**：内置响应式流流量控制
- **响应式生态系统集成**：与 Project Reactor、RxJava 无缝集成

### 同步模式优势

- **低延迟**：简单用例的直接阻塞 I/O
- **简单性**：传统编程模型
- **更好的调试**：直接的堆栈跟踪
- **更低的学习曲线**：大多数 Java 开发人员熟悉

## 模式间迁移

由于适配器模式，模式间迁移非常简单：

1. **从 SYNC 切换到 REACTIVE**：
   - 添加 `spring-boot-starter-webflux` 依赖
   - 如有需要，更新客户端配置
   - 应用逻辑无需代码更改

2. **从 REACTIVE 切换到 SYNC**：
   - 移除 `spring-boot-starter-webflux`
   - 添加 `spring-boot-starter-web`（如果尚未存在）
   - 应用逻辑无需代码更改

3. **使用 AUTO 模式以获得兼容性**：
   - 适用于两种依赖集
   - 自动选择合适的模式
   - 最适合库和多环境应用程序

## 最佳实践

### 模式选择指南

- **选择 REACTIVE 当**：
  - 构建高吞吐量微服务
  - 使用响应式数据库（R2DBC）
  - 需要处理数千个并发连接
  - 与响应式 API 集成

- **选择 SYNC 当**：
  - 构建简单的 CRUD 应用程序
  - 需要最大请求性能
  - 使用同步数据库（JDBC）
  - 开发传统 Web 应用程序

- **选择 AUTO 当**：
  - 构建需要广泛兼容性的库
  - 在不同部署环境中工作
  - 希望避免依赖冲突
  - 需要同时支持响应式和同步消费者

### 配置技巧

- 生产环境始终指定超时
- 分布式系统使用负载均衡器功能
- 为不可靠服务实现重试策略
- 启用日志记录以便调试 HTTP 交互
- 使用连接池以获得更好的性能

## 参考资料

1. [ClientMode.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/ClientMode.kt) - 客户端模式检测和枚举定义
2. [HttpExchangeAdapterFactory.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/HttpExchangeAdapterFactory.kt) - 适配器工厂的 SPI 接口
3. [ReactiveHttpExchangeAdapterFactory.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/ReactiveHttpExchangeAdapterFactory.kt) - WebClient 适配器工厂实现
4. [AbstractWebClientFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/AbstractWebClientFactoryBean.kt) - WebClient 配置基类
5. [AbstractRestClientFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/sync/AbstractRestClientFactoryBean.kt) - RestClient 配置基类
6. [Spring build.gradle.kts](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/build.gradle.kts) - Gradle 特性变体配置

## 相关页面

- [HTTP 客户端配置](../getting-started/configuration.md) - 详细的配置选项和属性
- [负载均衡](./load-balancing.md) - 负载均衡器集成和配置
- [重试机制](.md) - 重试策略和退避算法
- [认证](./authentication.md) - JWT 和其他认证方法
- [监控](.md) - HTTP 客户端的指标和日志记录
