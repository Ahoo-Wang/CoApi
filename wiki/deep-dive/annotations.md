---
title: Annotations
description: Deep dive into CoApi annotations including @CoApi, @LoadBalanced, and configuration parsing
---

# Annotations

## Overview

CoApi provides a sophisticated annotation-based configuration system that simplifies the integration of distributed service clients. These annotations enable developers to define service endpoints, configure load balancing, and manage service discovery with minimal boilerplate code. The annotation system is designed to be intuitive while providing powerful configuration options for various deployment scenarios.

## At-a-Glance

| Annotation | Target | Purpose | Key Parameters | Default Behavior |
|------------|--------|---------|---------------|-----------------|
| `@CoApi` | Class | Define a service client | `baseUrl`, `serviceId`, `name` | Auto-registers as @Component |
| `@LoadBalanced` | Class | Mark interface as load-balanced | (none) | Requires explicit annotation or `lb://` prefix |

## Core Annotations

### @CoApi Annotation

The `@CoApi` annotation is the cornerstone of the CoApi configuration system. It marks a class as a service client and provides essential configuration parameters.

```kotlin
@Target(AnnotationTarget.CLASS)
@Component
annotation class CoApi(
    val baseUrl: String = "",
    val serviceId: String = "",
    val name: String = ""
)
```

**Key Features:**
- **Automatic Component Registration**: The `@Component` meta-annotation ensures Spring picks up annotated classes during component scanning
- **Flexible URL Configuration**: Supports multiple URL resolution strategies
- **Placeholder Support**: Enables environment variable substitution using `${...}` syntax
- **Protocol Support**: Handles both `lb://` (load-balanced) and `http://` (direct) protocols

**Usage Examples:**

```kotlin
// Direct HTTP connection
@CoApi(baseUrl = "https://api.github.com")
interface GitHubApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssue(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}

// Load-balanced service with placeholder
@CoApi(baseUrl = "${github.url}")
interface GitHubApiClient {
    // ...
}

// Service-based load balancing
@CoApi(serviceId = "github-service")
interface GitHubApiClient {
    // ...
}

// Custom naming
@CoApi(name = "CustomApi", baseUrl = "lb://github-service")
interface CustomApiClient {
    // ...
}
```

### @LoadBalanced Annotation

The `@LoadBalanced` annotation provides explicit load balancing configuration:

```kotlin
@Target(AnnotationTarget.CLASS)
annotation class LoadBalanced
```

**Purpose:**
- Marks an interface as load-balanced even when not using `lb://` protocol
- Takes precedence over URL-based load balancing determination
- Useful for services that require load balancing but use direct HTTP connections

## URL Resolution Flow

The CoApi system employs a sophisticated URL resolution algorithm that determines the final service endpoint based on annotation configuration:

```mermaid
graph TD
    A[Start Class Processing] --> B{"Has @CoApi Annotation?"}
    B -->|No| C[Throw IllegalArgumentException]
    B -->|Yes| D[Resolve Base URL Logic]
    
    D --> E{baseUrl isNotBlank?}
    E -->|Yes| F[Resolve Placeholders]
    E -->|No| G{serviceId isNotBlank?}
    G -->|Yes| H["Construct lb:// + serviceId"]
    G -->|No| I[Empty URL]
    
    F --> J[Determine Load Balanced]
    H --> J
    I --> J
    
    J --> K{Load Balanced Check}
    K --> L["@LoadBalanced annotation present?"]
    K --> M{URL starts with lb://?}
    
    L -->|Yes| N[loadBalanced = true]
    M -->|Yes| N
    L -->|No| O[loadBalanced = false]
    M -->|No| O
    
    N --> P[Strip lb:// prefix]
    O --> Q[Keep original URL]
    
    P --> R[Construct CoApiDefinition]
    Q --> R
    
    R --> S[Generate Bean Names]
    S --> T[End]
```

## Class Hierarchy and Relationships

The annotation system creates a clear hierarchy of components that work together to provide service client functionality:

```mermaid
classDiagram
    class CoApiAnnotation {
        +String baseUrl
        +String serviceId
        +String name
    }
    
    class LoadBalancedAnnotation {
        <<annotation>>
    }
    
    class CoApiDefinition {
        +String name
        +Class apiType
        +String baseUrl
        +Boolean loadBalanced
        +String httpClientBeanName
        +String coApiBeanName
    }
    
    class Environment {
        +String resolvePlaceholders(String)
    }
    
    class Class {
        +toCoApiDefinition(Environment)
        +resolveClientName(CoApi)
        +getAnnotation(Class)
    }
    
    CoApiAnnotation --> Class : annotates
    LoadBalancedAnnotation --> Class : annotates
    Class --> CoApiDefinition : creates
    Environment --> CoApiDefinition : resolves placeholders
    CoApiDefinition --> Environment : uses
```

## Parameter Flow and Processing

The annotation processing follows a systematic flow to transform declarative configuration into runtime-ready service definitions:

```mermaid
sequenceDiagram
    participant C as Class
    participant E as Environment
    participant CAD as CoApiDefinition

    C->>C: getAnnotation(CoApi.class)
    alt No @CoApi annotation
        C->>CAD: throw IllegalArgumentException
    else Has @CoApi annotation
        C->>C: resolveClientName(coApi)
        C->>C: resolveBaseUrl(environment)

        alt baseUrl isNotBlank
            C->>E: resolvePlaceholders(baseUrl)
        else serviceId isNotBlank
            C->>E: resolvePlaceholders(serviceId)
            C->>C: construct lb:// + serviceId
        else both blank
            C->>C: return empty string
        end

        C->>C: determine loadBalanced
        C->>C: check @LoadBalanced annotation
        C->>C: check baseUrl starts with lb://
        C->>C: loadBalanced = annotationPresent or protocolBased

        C->>C: adjust baseUrl if loadBalanced
        alt baseUrl starts with lb://
            C->>C: strip lb:// prefix
        end

        C->>CAD: CoApiDefinition(name, apiType, baseUrl, loadBalanced)
    end
```

## Configuration Examples

### Test Case Analysis

The CoApi system includes comprehensive test cases that demonstrate various configuration scenarios:

```kotlin
// Test Case 1: lb:// protocol
@CoApi(baseUrl = "lb://order-service")
interface LBMockApi

// Result: loadBalanced=true, baseUrl="http://order-service"

// Test Case 2: serviceId configuration
@CoApi(serviceId = "order-service")
interface MockServiceApi

// Result: loadBalanced=true, baseUrl="http://order-service"

// Test Case 3: Empty configuration with @LoadBalanced
@CoApi
@LoadBalanced
interface MockEmptyApi

// Result: loadBalanced=true, baseUrl=""
```

### Real-world Usage Examples

**GitHub API Client with Environment Configuration:**
```kotlin
@CoApi(baseUrl = "${github.url}", name = "GitHubApi")
interface GitHubApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssue(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}
```

**Service API Client with Service Discovery:**
```kotlin
@CoApi(serviceId = "github-service")
interface ServiceApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssue(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}
```

## Bean Generation

The annotation system automatically generates Spring bean names based on the configuration:

```mermaid
graph LR
    A[CoApi Configuration] --> B[Name Resolution]
    B --> C{Custom Name Provided?}
    C -->|Yes| D[Use custom name]
    C -->|No| E[Use class simple name]
    D --> F[Generate Bean Names]
    E --> F
    
    F --> G["name + \".HttpClient\""]
    F --> H["name + \".CoApi\""]
    
    G --> I[HttpClient Bean]
    H --> J[CoApi Bean]
```

## Best Practices

1. **Use Descriptive Names**: Always provide meaningful `name` parameters when working with multiple services
2. **Leverage Environment Variables**: Use `${...}` placeholders for configuration that varies between environments
3. **Explicit Load Balancing**: Use `@LoadBalanced` for services that need load balancing regardless of protocol
4. **Protocol Selection**: Use `lb://` for service-discovery-based load balancing and `http://` for direct connections
5. **Error Handling**: Always ensure `@CoApi` annotation is present on service client interfaces

## References

### Source Files
- [api/src/main/kotlin/me/ahoo/coapi/api/CoApi.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/api/src/main/kotlin/me/ahoo/coapi/api/CoApi.kt) - Main CoApi annotation definition
- [api/src/main/kotlin/me/ahoo/coapi/api/LoadBalanced.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/api/src/main/kotlin/me/ahoo/coapi/api/LoadBalanced.kt) - Load balancing annotation
- [spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiDefinition.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiDefinition.kt) - Configuration parsing logic
- [spring/src/test/kotlin/me/ahoo/coapi/spring/CoApiDefinitionTest.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/test/kotlin/me/ahoo/coapi/spring/CoApiDefinitionTest.kt) - Test cases
- [example/example-consumer-client/src/main/kotlin/me/ahoo/coapi/example/consumer/client/GitHubApiClient.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-consumer-client/src/main/kotlin/me/ahoo/coapi/example/consumer/client/GitHubApiClient.kt) - Example implementation
- [example/example-consumer-client/src/main/kotlin/me/ahoo/coapi/example/consumer/client/ServiceApiClient.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-consumer-client/src/main/kotlin/me/ahoo/coapi/example/consumer/client/ServiceApiClient.kt) - Service discovery example

### Related Pages
- [Configuration](../getting-started/configuration.md) - Detailed configuration guide
- [Service Discovery](./load-balancing.md) - Load balancing and service discovery
- [Testing](.md) - Testing strategies for CoApi clients
- [Examples](./examples.md) - Complete usage examples
