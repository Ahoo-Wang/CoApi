---
title: Troubleshooting
description: CoApi startup errors and surprising runtime behavior, with their causes and fixes.
---

# Troubleshooting

CoApi checks its configuration at startup and fails with a message that names the client and the fix. Find your message below.

## Startup errors

### `Invalid coapi.mode value: [...]`

`coapi.mode` must be `REACTIVE`, `SYNC` or `AUTO` (case-insensitive). Fix the value or remove the property.

### `Duplicate CoApi name [...]`

Two clients resolve to the same name, either through the same `@CoApi(name)` or through interfaces with the same simple name in different packages. The name determines bean names and the `coapi.clients.<name>` key, so it must be unique. Give one of them a distinct `@CoApi(name = "...")`.

### `Could not resolve placeholder '...'`

A `${...}` placeholder in `@CoApi(baseUrl)`, `@CoApi(serviceId)` or a `CoApiDefinition` bean's `baseUrl` is not defined in the active environment. Define the property, or add a default: `${github.url:https://api.github.com}`.

### `CoApi [...] is load balanced, but Spring Cloud LoadBalancer is not on the classpath`

The client uses `serviceId`, an `lb://` URL or `@LoadBalanced`, or `coapi.clients.<name>.load-balanced=true` is set. Add `org.springframework.cloud:spring-cloud-starter-loadbalancer`. If this environment should call a fixed host instead, set `coapi.clients.<name>.base-url` to a plain URL, or set `coapi.clients.<name>.load-balanced=false`.

### `CoApiDefinition bean [...] is declared by a non-static @Bean method`

`CoApiDefinition` beans are read before normal bean post-processing. Declare the `@Bean` method `static` (Kotlin: `@JvmStatic` inside a `companion object`). See [Registering Clients](../deep-dive/auto-configuration.md#coapidefinition-beans).

### `NoSuchBeanDefinitionException: ... WebClient$Builder` or `RestClient$Builder`

The builder for the active client mode is missing. Add `spring-boot-starter-webclient` (reactive) or `spring-boot-starter-restclient` (sync), or set `coapi.mode` to the mode whose builder you have. See [Installation](./installation.md#http-client-builder).

### `NoUniqueBeanDefinitionException: ... WebClient$Builder` or `RestClient$Builder`

The application defines more than one builder, for example a Spring Cloud `@LoadBalanced` builder next to Boot's. Mark the one CoApi should use as `@Primary`.

### `JWT token has no exp claim`

`jwtToExpirableToken()` needs a JWT with an `exp` claim. For tokens without one, build an `ExpirableToken(token, expireAt)` with an explicit expiry.

## Client not found

`NoSuchBeanDefinitionException` for your client interface means it was never registered. Check:

1. It is an **interface** annotated with `@CoApi`. Classes are ignored.
2. It is in the `@SpringBootApplication` package or below, or in a package listed in `coapi.base-packages`, or listed in `@EnableCoApi(clients = [...])`.
3. `coapi.enabled` is not `false`, which turns off scanning.

## Runtime surprises

### The client uses `WebClient`, but I expected `RestClient` (or the reverse)

With `coapi.mode=AUTO`, Spring WebFlux on the classpath selects reactive mode, even in a servlet application. Set `coapi.mode` explicitly. The mode applies to all clients in the application. See [Client Modes](../deep-dive/client-modes.md).

### Filters or interceptors are applied twice, or leak between clients

CoApi configures the builder bean it gets from the context. Spring Boot's builders are prototype-scoped, so each client gets a fresh one. If you declare your own `WebClient.Builder` or `RestClient.Builder` bean, make it `@Scope("prototype")` as well. Otherwise every client mutates the same builder.

### A `serviceId` client fails with no available instances

Load balancing is working, but service discovery has no instances for that `serviceId`. Check your discovery client registration. For a static setup, list instances under `spring.cloud.discovery.client.simple.instances.<serviceId>`.

### A per-client property has no effect

The key under `coapi.clients` must match the client name exactly. If the interface sets `@CoApi(name = "GitHubApi")`, the key is `coapi.clients.GitHubApi`, not the interface name. Also check that you are configuring the list for the active mode: `reactive.filter` or `sync.interceptor`.

## Debug logging

CoApi logs each registered bean at `INFO` (`Register WebClient [GitHubApiClient.HttpClient].`). It also logs at `WARN` when it skips a bean because one with that name already exists. For more detail:

```yaml
logging:
  level:
    me.ahoo.coapi: DEBUG
```
