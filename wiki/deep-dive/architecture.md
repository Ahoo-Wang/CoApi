---
title: Architecture Overview
description: Deep dive into CoApi's modular architecture, registration flow, and design patterns
---

# Architecture Overview

CoApi's architecture is designed to provide a seamless, type-safe HTTP client framework that integrates deeply with Spring ecosystem while maintaining flexibility for various deployment scenarios. The architecture addresses the common challenges of REST API client development by providing automatic discovery, configuration management, and support for both reactive and synchronous programming models.

## Overview

CoApi exists to solve the fundamental problem of creating type-safe HTTP clients in Spring applications without the boilerplate code typically associated with manual HTTP client configuration. By leveraging Spring's auto-configuration capabilities and annotation-driven programming model, CoApi reduces the complexity of integrating with external services while providing enterprise-grade features like load balancing, circuit breaking, and configuration management.

The architecture follows a modular design that separates concerns across three main layers: the API layer (for interface definitions), the Spring integration layer (for dependency injection and lifecycle management), and the Spring Boot integration layer (for auto-configuration and sensible defaults).

## At-a-Glance

| Component | Responsibility | Key Features | Source |
|----------|---------------|---------------|--------|
| `@CoApi` | Interface definition and configuration | Type-safe HTTP clients, service discovery, load balancing | [CoApi.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/api/src/main/kotlin/me/ahoo/coapi/api/CoApi.kt#L61) |
| `AbstractCoApiRegistrar` | Base registration logic | Client mode inference, factory registration | [AbstractCoApiRegistrar.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/AbstractCoApiRegistrar.kt#L28) |
| `CoApiRegistrar` | Individual client registration | Bean definition creation, factory bean registration | [CoApiRegistrar.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiRegistrar.kt#L22) |
| `CoApiFactoryBean` | Proxy creation | JDK proxy generation, service proxy factory | [CoApiFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiFactoryBean.kt#L21) |
| `HttpClientFactoryBean` | HTTP client configuration | WebClient/RestClient creation, filter application | [WebClientFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/WebClientFactoryBean.kt#L20) |

## Module Architecture

```mermaid
graph TB
    subgraph "CoApi Module Architecture"
        subgraph "API Layer"
            A["@CoApi Annotation"]
            B[CoApiDefinition]
            C[HttpExchangeAdapter]
        end
        
        subgraph "Spring Integration Layer"
            D[AbstractCoApiRegistrar]
            E[CoApiRegistrar]
            F[CoApiFactoryBean]
            G[WebClientFactoryBean]
            H[RestClientFactoryBean]
            I[HttpExchangeAdapterFactory]
        end
        
        subgraph "Spring Boot Integration Layer"
            J[AutoCoApiRegistrar]
            K[EnableCoApiRegistrar]
            L[CoApiProperties]
            M[ApiClientScanner]
        end
        
        subgraph "Client Adapters"
            N[ReactiveHttpExchangeAdapterFactory]
            O[SyncHttpExchangeAdapterFactory]
        end
        
        subgraph "HTTP Clients"
            P[WebClient]
            Q[RestClient]
        end
    end
    
    A --> B
    B --> C
    D --> E
    E --> F
    F --> G
    F --> H
    G --> I
    H --> I
    J --> D
    K --> D
    L --> J
    L --> K
    M --> J
    M --> K
    I --> N
    I --> O
    N --> P
    O --> Q
    
```

## Registration Flow

The registration process is the heart of CoApi's architecture, automatically discovering and configuring HTTP clients based on annotations and configuration. This sequence diagram illustrates the complete registration flow:

```mermaid
sequenceDiagram
    participant U as User Code
    participant A as CoApi Annotation
    participant B as AutoCoApiRegistrar
    participant C as AbstractCoApiRegistrar
    participant D as CoApiRegistrar
    participant E as CoApiFactoryBean
    participant F as WebClientFactoryBean
    participant G as RestClientFactoryBean
    participant H as HttpServiceProxyFactory
    participant I as JDK Proxy
    
    autonumber
    
    U->>A: @CoApi(baseUrl, serviceId, name)
    A->>B: Interface discovery
    B->>C: registerBeanDefinitions()
    C->>C: inferClientMode()
    C->>C: registerHttpExchangeAdapterFactory()
    C->>D: CoApiRegistrar(registry, clientMode)
    D->>D: register(coApiDefinitions)
    D->>F: registerWebClient() or registerRestClient()
    D->>E: registerApiClient()
    F->>G: getObject()
    E->>H: createClient()
    H->>I: create JDK Proxy
    I->>U: Return typed interface
    
```

<!-- Sources: [AutoCoApiRegistrar.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring-boot-starter/src/main/kotlin/me/ahoo/coapi/spring/boot/starter/AutoCoApiRegistrar.kt#L28), [AbstractCoApiRegistrar.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/AbstractCoApiRegistrar.kt#L42), [CoApiRegistrar.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiRegistrar.kt#L27), [CoApiFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiFactoryBean.kt#L26), [WebClientFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/WebClientFactoryBean.kt#L23) -->

## Bean Lifecycle

The Spring bean lifecycle is carefully managed to ensure proper initialization and dependency injection:

```mermaid
sequenceDiagram
    participant C as Spring Context
    participant R as Bean Definition Registry
    participant F as FactoryBean
    participant A as Application Context
    participant L as Load Balancer
    
    autonumber
    
    C->>R: registerBeanDefinitions()
    R->>F: Create FactoryBean
    F->>A: setApplicationContext()
    F->>A: getObject()
    alt Reactive Mode
        F->>A: getBean(ReactiveHttpExchangeAdapterFactory)
        A->>F: Return factory
    else Sync Mode
        F->>A: getBean(SyncHttpExchangeAdapterFactory)
        A->>F: Return factory
    end
    F->>F: create(HttpExchangeAdapter)
    F->>F: build(HttpServiceProxyFactory)
    F->>F: createClient(apiType)
    F->>C: Return proxy
    alt Load Balanced
        F->>L: Load balance requests
    end
    C->>F: Client ready for use
    
```

<!-- Sources: [CoApiFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiFactoryBean.kt#L40), [AbstractCoApiRegistrar.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/AbstractCoApiRegistrar.kt#L52), [CoApiFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiFactoryBean.kt#L26) -->

## Class Diagram

The class hierarchy shows the relationships between key components:

```mermaid
classDiagram
    class CoApi {
        +String baseUrl
        +String serviceId
        +String name
    }
    
    class CoApiDefinition {
        +String coApiBeanName
        +String httpClientBeanName
        +Class apiType
        +ClientMode clientMode
        +boolean loadBalanced
    }
    
    class AbstractCoApiRegistrar {
        #Environment env
        #BeanFactory appContext
        +getCoApiDefinitions(AnnotationMetadata)
        +registerBeanDefinitions(AnnotationMetadata, registry)
    }
    
    class CoApiRegistrar {
        -BeanDefinitionRegistry registry
        -ClientMode clientMode
        +register(Set~CoApiDefinition~)
        +register(CoApiDefinition)
    }
    
    class CoApiFactoryBean {
        -CoApiDefinition coApiDefinition
        +getObject()
        +getObjectType()
    }
    
    class WebClientFactoryBean {
        +getObject()
        +loadBalanced()
    }
    
    class RestClientFactoryBean {
        +getObject()
        +loadBalanced()
    }
    
    class HttpServiceProxyFactory {
        +createClient(Class)
    }
    
    class ReactiveHttpExchangeAdapterFactory {
        +create(BeanFactory, String)
    }
    
    class SyncHttpExchangeAdapterFactory {
        +create(BeanFactory, String)
    }
    
    CoApi --> CoApiDefinition : creates
    AbstractCoApiRegistrar --> CoApiDefinition : uses
    AbstractCoApiRegistrar --> CoApiRegistrar : creates
    CoApiRegistrar --> WebClientFactoryBean : registers
    CoApiRegistrar --> RestClientFactoryBean : registers
    CoApiRegistrar --> CoApiFactoryBean : registers
    CoApiFactoryBean --> HttpServiceProxyFactory : uses
    CoApiFactoryBean --> ReactiveHttpExchangeAdapterFactory : gets
    CoApiFactoryBean --> SyncHttpExchangeAdapterFactory : gets
    WebClientFactoryBean --> ReactiveHttpExchangeAdapterFactory : uses
    RestClientFactoryBean --> SyncHttpExchangeAdapterFactory : uses
    
```

<!-- Sources: [CoApi.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/api/src/main/kotlin/me/ahoo/coapi/api/CoApi.kt#L63), [CoApiDefinition.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiDefinition.kt), [AbstractCoApiRegistrar.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/AbstractCoApiRegistrar.kt#L28), [CoApiRegistrar.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiRegistrar.kt#L22), [CoApiFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiFactoryBean.kt#L21), [WebClientFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/WebClientFactoryBean.kt#L20), [RestClientFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/sync/RestClientFactoryBean.kt#L21) -->

## Layered Architecture

The architecture follows a clean layered approach with clear separation of concerns:

```mermaid
graph TD
    subgraph "Application Layer"
        A[User Interface]
        B[Business Logic]
        C[CoApi Clients]
    end
    
    subgraph "Service Layer"
        D[Service Interfaces]
        E[Service Implementations]
        F[Domain Services]
    end
    
    subgraph "Infrastructure Layer"
        G[CoApi Framework]
        H[HTTP Clients]
        I[Load Balancers]
        J[Configuration]
    end
    
    subgraph "Framework Layer"
        K[Spring Framework]
        L[Spring Boot]
        M[Spring Cloud]
    end
    
    A --> B
    B --> C
    C --> D
    D --> E
    E --> F
    F --> G
    G --> H
    H --> I
    I --> J
    J --> K
    K --> L
    L --> M
    
```

<!-- Sources: [CoApi.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/api/src/main/kotlin/me/ahoo/coapi/api/CoApi.kt#L14), [AbstractCoApiRegistrar.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/AbstractCoApiRegistrar.kt#L14), [CoApiRegistrar.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiRegistrar.kt#L14) -->

## Key Design Patterns

CoApi's architecture implements several design patterns to ensure maintainability and extensibility:

### 1. Factory Pattern
Factory beans are used to create complex objects like HTTP clients and proxies with proper configuration.

### 2. Strategy Pattern
Different client modes (reactive vs sync) are handled using the Strategy pattern with different factory implementations.

### 3. Template Method Pattern
Abstract factory classes provide common functionality while allowing specific implementations.

### 4. Proxy Pattern
JDK proxies are used to create type-safe interfaces that delegate to HTTP clients.

### 5. Builder Pattern
HttpServiceProxyFactory and client builders use the Builder pattern for fluent configuration.

## Cross-References

- [Getting Started](../getting-started/index.md) - Introduction to CoApi basics
- [Configuration Reference](../getting-started/configuration.md) - Complete configuration guide
- [Client Modes](
- [Spring Boot Integration](.md) - Spring Boot specific patterns
- [Annotations](./annotations/annotations.md) - Annotation-based configuration

## References

### Source Files

- [CoApi.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/api/src/main/kotlin/me/ahoo/coapi/api/CoApi.kt) - Main annotation interface
- [AutoCoApiRegistrar.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring-boot-starter/src/main/kotlin/me/ahoo/coapi/spring/boot/starter/AutoCoApiRegistrar.kt) - Auto-configuration registration
- [EnableCoApiRegistrar.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/EnableCoApiRegistrar.kt) - Manual registration
- [AbstractCoApiRegistrar.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/AbstractCoApiRegistrar.kt) - Base registration logic
- [CoApiRegistrar.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiRegistrar.kt) - Individual client registration
- [CoApiFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiFactoryBean.kt) - Proxy creation factory
- [WebClientFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/WebClientFactoryBean.kt) - Reactive HTTP client factory
- [RestClientFactoryBean.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/sync/RestClientFactoryBean.kt) - Synchronous HTTP client factory

### Related Pages

- [Module Architecture](
- [Registration Process](
- [Bean Lifecycle](
- [Design Patterns](
- [Performance Considerations](.md) - Performance optimization guidelines
