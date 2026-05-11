---
title: Configuration Reference
description: Complete reference for CoApi configuration properties, resolution order, and advanced patterns
---

# 配置参考

CoApi 的配置系统旨在提供最大的灵活性，同时保持合理的默认值和清晰的优先级规则。配置采用分层方法，允许全局设置和特定于客户端的覆盖，使开发人员能够跨整个 API 客户端生态系统或针对各个服务自定义行为。

## 概述

CoApi 的配置架构在声明式便利性和程序化控制之间取得平衡。通过支持注解驱动和基于属性的配置，它适应不同的开发风格和部署场景。系统优先考虑显式属性声明，同时为向后兼容性和快速原型设计提供注解回退。

## 配置属性

### 全局属性

| 属性 | 类型 | 默认 | 描述 | 来源 |
|----------|------|---------|-------------|--------|
| `coapi.enabled` | `Boolean` | `true` | 启用/禁用 CoApi 功能 | [CoApiProperties.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring-boot-starter/src/main/kotlin/me/ahoo/coapi/spring/boot/starter/CoApiProperties.kt#L1) |
| `coapi.mode` | `ClientMode` | `AUTO` | 全局客户端模式（AUTO、REACTIVE、SYNC） | [CoApiProperties.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring-boot-starter/src/main/kotlin/me/ahoo/coapi/spring/boot/starter/CoApiProperties.kt#L2) |
| `coapi.base-packages` | `List<String>` | `[]` | 客户端发现的基础包 | [CoApiProperties.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring-boot-starter/src/main/kotlin/me/ahoo/coapi/spring/boot/starter/CoApiProperties.kt#L3) |

### 客户端属性

| 属性 | 类型 | 默认 | 描述 | 来源 |
|----------|------|---------|-------------|--------|
| `coapi.clients.<name>.base-url` | `String` | `""` | 客户端的基础 URL | [ClientProperties.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/ClientProperties.kt#L1) |
| `coapi.clients.<name>.load-balanced` | `Boolean?` | `null` | 为客户端启用负载均衡 | [ClientProperties.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/ClientProperties.kt#L2) |

### 响应式客户端属性

| 属性 | 类型 | 默认 | 描述 | 来源 |
|----------|------|---------|-------------|--------|
| `coapi.clients.<name>.reactive.filter.names` | `List<String>` | `[]` | 响应式过滤器函数名称 | [ClientProperties.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/ClientProperties.kt#L1) |
| `coapi.clients.<name>.reactive.filter.types` | `List<String>` | `[]` | 响应式过滤器函数类型 | [ClientProperties.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/ClientProperties.kt#L2) |

### 同步客户端属性

| 属性 | 类型 | 默认 | 描述 | 来源 |
|----------|------|---------|-------------|--------|
| `coapi.clients.<name>.sync.interceptor.names` | `List<String>` | `[]` | 同步拦截器名称 | [SyncClientDefinition.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/ClientProperties.kt#L2) |

## 配置解析流程

配置系统遵循严格的优先级顺序以确保可预测的行为：

```mermaid
flowchart TD
    A[Start Configuration Resolution] --> B{"Check Properties File"}
    B -->|Has coapi.clients.<name>.base-url| C[Use Properties baseUrl]
    B -->|No properties baseUrl| D{Check @CoApi Annotation}
    D -->|Has baseUrl| E[Use Annotation baseUrl]
    D -->|No annotation baseUrl| F[Throw Configuration Exception]

    A --> G{Check coapi.clients.<name>.load-balanced}
    G -->|Has property| H[Use Properties loadBalanced]
    G -->|No property| I{Check @LoadBalanced Annotation}
    I -->|Has annotation| J[Use Annotation loadBalanced]
    I -->|No annotation| K[Use Default Behavior]

    C --> L[Resolve Complete Configuration]
    E --> L
    H --> L
    J --> L
    K --> L

```

## 属性层次结构

配置层次结构决定了不同配置源的合并和优先级：

```mermaid
graph TD
    subgraph "Global Level"
        A[coapi.enabled]
        B[coapi.mode]
        C[coapi.base-packages]
    end

    subgraph "Client Level"
        D[coapi.clients.<name>.base-url]
        E[coapi.clients.<name>.load-balanced]
    end

    subgraph "Client Sub-Level"
        F[coapi.clients.<name>.reactive.filter.*]
        G[coapi.clients.<name>.sync.interceptor.*]
    end

    subgraph "Annotation Level"
        H["@CoApi(baseUrl)"]
        I["@LoadBalanced"]
    end

    A --> D
    B --> D
    C --> D
    D --> F
    D --> G
    E --> F
    E --> G
    H --> D
    I --> E

```

## 客户端配置示例

一个完整的客户端配置示例，显示所有可用选项：

```mermaid
graph TB
    subgraph "Application.yml"
        A["coapi:"]
    end

    subgraph "Global Settings"
        B["enabled: true"]
        C["mode: AUTO"]
        D["base-packages:"]
        E[ - com.example.clients]
    end

    subgraph "Client Definitions"
        F["clients:"]
        G["GitHubApiClient:"]
        H["base-url: https://api.github.com"]
        I["ServiceApiClient:"]
        J["load-balanced: true"]
        K["reactive:"]
        L["filter:"]
        M["names:"]
        N[ - loadBalancerExchangeFilterFunction]
        O["types:"]
        P[" - org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction"]
        Q["sync:"]
        R["interceptor:"]
        S["names:"]
        T[ - loadBalancerInterceptor]
    end

    A --> B
    A --> C
    A --> D
    D --> E
    A --> F
    F --> G
    G --> H
    F --> I
    I --> J
    I --> K
    K --> L
    L --> M
    M --> N
    L --> O
    O --> P
    I --> Q
    Q --> R
    R --> S
    S --> T

```

## 配置解析序列

解析过程遵循明确定义的序列以确保可预测的行为：

```mermaid
sequenceDiagram
    participant P as Properties File
    participant A as Annotations
    participant F as FactoryBean
    participant C as Client Instance

    autonumber

    F->>P: Check coapi.clients.<name>.base-url
    alt Has property
        P-->>F: Return baseUrl from properties
    else No property
        F->>A: Check @CoApi annotation
        alt Has annotation
            A-->>F: Return baseUrl from annotation
        else No annotation
            F-->>F: Throw ConfigurationException
        end
    end

    F->>P: Check coapi.clients.<name>.load-balanced
    alt Has property
        P-->>F: Return loadBalanced from properties
    else No property
        F->>A: Check @LoadBalanced annotation
        alt Has annotation
            A-->>F: Return loadBalanced from annotation
        else No annotation
            F-->>F: Use default behavior
        end
    end

    F->>F: Build ClientDefinition
    F-->>C: Return configured client
```

## YAML 配置示例

```yaml
coapi:
  enabled: true
  mode: AUTO  # AUTO, REACTIVE, SYNC
  base-packages:
    - com.example.clients
  clients:
    GitHubApiClient:
      base-url: https://api.github.com
    ServiceApiClient:
      load-balanced: true
      reactive:
        filter:
          names:
            - loadBalancerExchangeFilterFunction
          types:
            - org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction
      sync:
        interceptor:
          names:
            - loadBalancerInterceptor
```

## 交叉引用

- [客户端模式](/zh/getting-started/overview.md) - 不同客户端操作模式的详细信息
- [架构概述](/zh/deep-dive/architecture.md) - 深入了解注册流程

## 参考资料

### 源文件

- [CoApiProperties.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring-boot-starter/src/main/kotlin/me/ahoo/coapi/spring/boot/starter/CoApiProperties.kt) - 主配置属性类
- [AbstractHttpClientFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/AbstractHttpClientFactoryBean.kt) - 配置解析逻辑
- [ClientProperties.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/ClientProperties.kt) - 客户端配置类
- [ClientMode.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/ClientMode.kt) - 客户端模式枚举
- [ConditionalOnCoApiEnabled.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring-boot-starter/src/main/kotlin/me/ahoo/coapi/spring/boot/starter/ConditionalOnCoApiEnabled.kt) - 条件配置

### 相关页面

- [概述](/zh/getting-started/overview.md) - CoApi 基础介绍
- [配置参考](/zh/getting-started/configuration.md) - 完整配置指南
