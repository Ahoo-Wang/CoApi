---
title: Examples & Patterns
description: CoApi usage examples and implementation patterns
---

# Examples & Patterns

## Overview

CoApi provides flexible patterns for building type-safe HTTP clients and servers. This page explores practical examples covering provider-consumer architectures, third-party API integration, filter configuration, sync clients, and connection pool customization. These patterns demonstrate how CoApi maintains consistency between contracts and implementations while supporting various deployment scenarios.

## At a Glance

| Pattern | Key Components | Use Case | Key Benefits |
|---------|---------------|----------|-------------|
| Provider-Consumer | Shared API, Provider Server, Consumer Server | Internal microservices | Single contract prevents inconsistency |
| Third-Party API | @CoApi with different configurations | External service integration | Flexible URL and load-balancing options |
| Filter Configuration | YAML-based filtering | Service selection | Fine-grained client routing control |
| Sync Java | @EnableCoApi with Java clients | Synchronous operations | Traditional Java integration |
| Connection Pool | WebClientBuilderCustomizer | Performance tuning | Per-client resource optimization |

## Provider-Consumer Pattern

The primary pattern involves a shared API contract that prevents inconsistency between provider and consumer services.

```mermaid
classDiagram
    class TodoApi {
        +createTodo(todo: Todo): Mono~Todo~
        +getTodos(): Flux~Todo~
        +getTodo(id: String): Mono~Todo~
        +updateTodo(id: String, todo: Todo): Mono~Todo~
        +deleteTodo(id: String): Mono~Void~
    }
    class TodoController {
        +createTodo(todo: Todo): Mono~Todo~
        +getTodos(): Flux~Todo~
        +getTodo(id: String): Mono~Todo~
        +updateTodo(id: String, todo: Todo): Mono~Todo~
        +deleteTodo(id: String): Mono~Void~
    }
    class TodoClient {
        +createTodo(todo: Todo): Mono~Todo~
        +getTodos(): Flux~Todo~
        +getTodo(id: String): Mono~Todo~
        +updateTodo(id: String, todo: Todo): Mono~Todo~
        +deleteTodo(id: String): Mono~Void~
    }
    class ConsumerServer {
        +todoClient: TodoClient
        +useTodoClient(): void
    }
    
    TodoApi <|-- TodoController : implements
    TodoApi <|.. TodoClient : @CoApi
    TodoClient --> ConsumerServer : inject
```

**Components:**

1. **Shared API Module** (`example-provider-api`) - Defines the contract
   - [`TodoApi.kt`](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-provider-api/src/main/kotlin/me/ahoo/coapi/example/provider/api/TodoApi.kt) with `@HttpExchange` annotations

2. **Provider Server** (`example-provider-server`) - Implements the contract
   - [`TodoController.kt`](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-provider-server/src/main/kotlin/me/ahoo/coapi/example/provider/TodoController.kt) implements `TodoApi`

3. **Consumer Server** (`example-consumer-server`) - Uses the client
   - Injects `TodoClient` and calls methods defined in `TodoApi`

**Benefit:** Single contract prevents inconsistency between provider and consumer implementations.

## Third-Party API Client

CoApi supports multiple approaches for integrating third-party APIs:

```mermaid
sequenceDiagram
    participant Consumer
    participant CoApi
    participant GitHubApi
    participant LoadBalancer
    participant ServiceRegistry
    
    Consumer->>CoApi: @CoApi(baseUrl="${github.url}")
    CoApi->>GitHubApi: @GetExchange /repos/{owner}/{repo}
    GitHubApi-->>Consumer: List<Issue>
    
    Consumer->>CoApi: @CoApi(serviceId="github-service")
    CoApi->>LoadBalancer: request routing
    LoadBalancer->>ServiceRegistry: lookup service
    ServiceRegistry-->>LoadBalancer: service instances
    LoadBalancer->>GitHubApi: load-balanced request
    GitHubApi-->>Consumer: List<Issue>
    
    Consumer->>CoApi: @CoApi (no URL)
    CoApi->>Consumer: URI or UriBuilderFactory
    Consumer->>GitHubApi: direct URI usage
    GitHubApi-->>Consumer: List<Issue>
```

**Client Types:**

1. **GitHubApiClient** - Direct base URL configuration
   - [`GitHubApiClient.kt`](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-consumer-client/src/main/kotlin/me/ahoo/coapi/example/consumer/client/GitHubApiClient.kt)
   - `@CoApi(baseUrl = "${github.url}")` with `@GetExchange`

2. **ServiceApiClient** - Load-balanced service discovery
   - [`ServiceApiClient.kt`](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-consumer-client/src/main/kotlin/me/ahoo/coapi/example/consumer/client/ServiceApiClient.kt)
   - `@CoApi(serviceId = "github-service", name = "GitHubApi")`

3. **UriApiClient** - Direct URI usage
   - [`UriApiClient.kt`](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-consumer-client/src/main/kotlin/me/ahoo/coapi/example/consumer/client/UriApiClient.kt)
   - `@CoApi` (no URL) - uses `URI` or `UriBuilderFactory` directly

## Filter Configuration Patterns

CoApi provides flexible filtering mechanisms for service selection:

```mermaid
graph TD
    subgraph Filter Configuration
        A[YAML Configuration] --> B[Filter by Bean Name]
        A --> C[Filter by Class Type]
    end
    
    B --> D[ServiceApiClientUseFilterBeanName]
    C --> E[ServiceApiClientUseFilterType]
    
    D --> F[Filter via bean name in YAML]
    E --> G[Filter via class type in YAML]
    
```

**Filter Types:**

1. **ServiceApiClientUseFilterBeanName** - Filter by bean name via YAML
2. **ServiceApiClientUseFilterType** - Filter by class type via YAML

Both patterns allow fine-grained control over service selection in complex deployments.

## Sync Java Example

CoApi supports both reactive and synchronous Java clients:

```mermaid
classDiagram
    class GitHubSyncClient {
        +getIssues(): List~Issue~
        +getIssue(id: String): Issue
    }
    class GitHubSyncLbClient {
        +getIssues(): List~Issue~
        +getIssue(id: String): Issue
    }
    class ExampleSyncServer {
        +githubSyncClient: GitHubSyncClient
        +githubSyncLbClient: GitHubSyncLbClient
        +startup(): void
    }
    
    GitHubSyncClient --> ExampleSyncServer : inject
    GitHubSyncLbClient --> ExampleSyncServer : inject
    ExampleSyncServer ..> GitHubSyncClient : @EnableCoApi
    ExampleSyncServer ..> GitHubSyncLbClient : @EnableCoApi
```

**Components:**

1. **GitHubSyncClient** (Java) - Direct URL configuration
   - Returns `List<Issue>`

2. **GitHubSyncLbClient** (Java) - Load-balanced configuration  
   - Returns `List<Issue>`

3. **ExampleSyncServer** - Configuration
   - [`GitHubSyncClient.java`](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-sync/src/main/java/me/ahoo/coapi/example/sync/GitHubSyncClient.java)
   - `@EnableCoApi(clients = [GitHubSyncClient::class])`

## Connection Pool Customization

For performance optimization, CoApi allows per-client connection pool configuration:

```mermaid
graph TD
    subgraph Connection Pool Configuration
        A[ConsumerWebClientBuilderCustomizer] --> B[ConnectionProvider.builder]
        B --> C[coApiDefinition.name]
        C --> D[Per-client pool settings]
    end
    
    D --> E[Max connections]
    D --> F[Acquire timeout]
    D --> G[Idle timeout]
    
```

**Implementation:**
- [`ConsumerWebClientBuilderCustomizer.kt`](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-consumer-server/src/main/kotlin/me/ahoo/coapi/example/consumer/ConsumerWebClientBuilderCustomizer.kt)
- Uses `ConnectionProvider.builder(coApiDefinition.name)` for per-client configuration

## References

1. [TodoApi Interface](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-provider-api/src/main/kotlin/me/ahoo/coapi/example/provider/api/TodoApi.kt) - Shared API contract definition
2. [TodoClient Interface](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-provider-api/src/main/kotlin/me/ahoo/coapi/example/provider/client/TodoClient.kt) - Consumer-side client implementation
3. [TodoController](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-provider-server/src/main/kotlin/me/ahoo/coapi/example/provider/TodoController.kt) - Provider-side controller implementation
4. [GitHubApiClient](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-consumer-client/src/main/kotlin/me/ahoo/coapi/example/consumer/client/GitHubApiClient.kt) - Third-party API client with base URL
5. [ServiceApiClient](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-consumer-client/src/main/kotlin/me/ahoo/coapi/example/consumer/client/ServiceApiClient.kt) - Load-balanced service client
6. [UriApiClient](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-consumer-client/src/main/kotlin/me/ahoo/coapi/example/consumer/client/UriApiClient.kt) - URI-based client
7. [ConsumerServer](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-consumer-server/src/main/kotlin/me/ahoo/coapi/example/consumer/ConsumerServer.kt) - Consumer application configuration
8. [ConsumerWebClientBuilderCustomizer](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-consumer-server/src/main/kotlin/me/ahoo/coapi/example/consumer/ConsumerWebClientBuilderCustomizer.kt) - Connection pool customization
9. [GitHubSyncClient](https://github.com/Ahoo-Wang/CoApi/blob/main/example/example-sync/src/main/java/me/ahoo/coapi/example/sync/GitHubSyncClient.java) - Synchronous Java client

## Related Pages

- [Getting Started](../getting-started.md) - Basic setup and configuration
- [Configuration](../getting-started/configuration.md) - Detailed configuration options
- [Advanced Topics](.md) - Advanced patterns and customizations
- [Best Practices](.md) - Recommended approaches and patterns
- [Troubleshooting](.md) - Common issues and solutions
