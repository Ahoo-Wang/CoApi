---
title: Examples
description: The runnable example applications in the CoApi repository and the pattern each one demonstrates.
---

# Examples

The [`example/`](https://github.com/Ahoo-Wang/CoApi/tree/main/example) directory has runnable applications. Every pattern in this wiki appears in one of them.

| Module | Demonstrates |
|--------|--------------|
| [`example-provider-api`](https://github.com/Ahoo-Wang/CoApi/tree/main/example/example-provider-api) | Shared contract: `TodoApi` (`@HttpExchange`) and `TodoClient : TodoApi` (`@CoApi(serviceId = "provider-service")`), depending only on `coapi-api` |
| [`example-provider-server`](https://github.com/Ahoo-Wang/CoApi/tree/main/example/example-provider-server) | Provider implementing `TodoApi` in a `@RestController` (port 8010) |
| [`example-consumer-client`](https://github.com/Ahoo-Wang/CoApi/tree/main/example/example-consumer-client) | Client library: placeholder `baseUrl`, `serviceId` with custom `name`, per-client filters, a client without base URL taking `URI`/`UriBuilderFactory` |
| [`example-consumer-server`](https://github.com/Ahoo-Wang/CoApi/tree/main/example/example-consumer-server) | Reactive consumer: `@EnableCoApi` plus `coapi.base-packages`, static discovery instances, filters by bean name and by type, a per-client connection pool customizer |
| [`example-sync`](https://github.com/Ahoo-Wang/CoApi/tree/main/example/example-sync) | Synchronous Java clients (`List<Issue>` return types) on `RestClient`, with and without load balancing |

## Running them

The tests start each application against local stand-ins, so they need no network:

```bash
./gradlew :example-consumer-server:test :example-provider-server:test :example-sync:test
```

To try the provider/consumer pair by hand, start `ProviderServer` (port 8010) and `ConsumerServer` (port 8080) from your IDE, then call `GET http://localhost:8080/todo`. The consumer resolves `provider-service` through the simple discovery instances in its `application.yaml`.

## Where each pattern is explained

| Pattern | Page |
|---------|------|
| Shared provider/consumer contract | [Defining Clients](./annotations.md#shared-contracts) |
| Clients from another module | [Registering Clients](./auto-configuration.md) |
| Filters by name/type, connection pool customizer | [Customization](./customization.md) |
| `serviceId` with static instances | [Load Balancing](./load-balancing.md) |
| Sync clients in Java | [Client Modes](./client-modes.md) |
