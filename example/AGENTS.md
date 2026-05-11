# AGENTS.md — example Modules

## Build & Run

```bash
# Provider server
./gradlew :example-provider-server:build
./gradlew :example-provider-server:test

# Consumer server
./gradlew :example-consumer-server:build
./gradlew :example-consumer-server:test

# Sync example
./gradlew :example-sync:build
./gradlew :example-sync:test
```

## Module Structure

| Module | Role |
|--------|------|
| `example-provider-api` | Shared `TodoApi` interface + `TodoClient` |
| `example-provider-server` | Server implementing `TodoApi` via `TodoController` |
| `example-consumer-client` | Client interfaces: `GitHubApiClient`, `ServiceApiClient`, `UriApiClient` |
| `example-consumer-server` | Consumer app using `@EnableCoApi` with WebClient customization |
| `example-sync` | Synchronous Java client example using `RestClient` |

## Key Patterns Demonstrated

- **Provider-Consumer**: Shared API module, `TodoClient extends TodoApi`
- **Third-party API**: `@CoApi(baseUrl = "${github.url}")` with property placeholder
- **Load balancing**: `@CoApi(serviceId = "github-service")` with SimpleDiscoveryClient
- **Filter configuration**: Per-client filter by bean name and by class type
- **Sync mode**: Java interfaces returning `List<T>` instead of `Flux<T>`
- **Connection pool**: `ConsumerWebClientBuilderCustomizer` with per-client `ConnectionProvider`

## Boundaries

- ✅ Add new example interfaces and controllers
- ✅ Update YAML configuration examples
- ⚠️ Changes may break CI example-test workflow
- 🚫 Do not remove existing examples (referenced by integration tests)
