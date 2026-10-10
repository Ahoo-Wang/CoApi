---
title: Configuration Reference
description: Every coapi.* property, and the rules for how per-client properties override what the @CoApi annotation declares.
---

# Configuration Reference

All properties live under the `coapi` prefix and are bound by the Spring Boot starter ([`CoApiProperties`](https://github.com/Ahoo-Wang/CoApi/blob/main/spring-boot-starter/src/main/kotlin/me/ahoo/coapi/spring/boot/starter/CoApiProperties.kt)).

## Global properties

| Property | Default | Description |
|----------|---------|-------------|
| `coapi.enabled` | `true` | `false` turns off Spring Boot auto-configuration (classpath scanning and `coapi.*` binding). Clients listed in `@EnableCoApi` are still registered. |
| `coapi.mode` | `AUTO` | `REACTIVE` (`WebClient`), `SYNC` (`RestClient`) or `AUTO` (inferred from the classpath). Case-insensitive; an invalid value fails startup. See [Client Modes](../deep-dive/client-modes.md). |
| `coapi.base-packages` | empty | Extra packages to scan for `@CoApi` interfaces, in addition to the `@SpringBootApplication` package. Accepts a comma-separated string or a YAML list. |

## Per-client properties

`<name>` is the client name: `@CoApi(name)` if set, otherwise the interface's simple name (`GitHubApiClient`). Renaming the client changes its configuration key.

| Property | Default | Description |
|----------|---------|-------------|
| `coapi.clients.<name>.base-url` | unset | Replaces the annotation's base URL. An `lb://` URL enables load balancing. |
| `coapi.clients.<name>.load-balanced` | unset | `true` or `false` forces load balancing on or off. |
| `coapi.clients.<name>.reactive.filter.names` | empty | Bean names of `ExchangeFilterFunction`s to add (reactive mode). |
| `coapi.clients.<name>.reactive.filter.types` | empty | Bean types (fully qualified class names) of `ExchangeFilterFunction`s to add. |
| `coapi.clients.<name>.sync.interceptor.names` | empty | Bean names of `ClientHttpRequestInterceptor`s to add (sync mode). |
| `coapi.clients.<name>.sync.interceptor.types` | empty | Bean types of `ClientHttpRequestInterceptor`s to add. |

Filters and interceptors must be beans. They are applied by name first, then by type, each in the configured order. A type must resolve to exactly one bean. Only the list for the active mode is used: `reactive.*` in reactive mode, `sync.*` in sync mode.

## Override rules

The annotation defines a client. Per-client properties override it when the HTTP client is built. The result is the client's **effective definition**, which is also what [builder customizers](../deep-dive/customization.md) receive.

**Base URL**

1. `coapi.clients.<name>.base-url`, if not blank.
2. Otherwise `@CoApi(baseUrl)`, with placeholders resolved.
3. Otherwise `lb://` + `@CoApi(serviceId)`.
4. Otherwise empty. The client must then pass a full `URI` per request (see [Defining Clients](../deep-dive/annotations.md#no-base-url)).

An `lb://` scheme (case-insensitive) is always rewritten to `http://` and marks the client load balanced.

**Load balancing**

| `load-balanced` | `base-url` | Load balanced? |
|-----------------|------------|----------------|
| `true` / `false` | any | the configured value |
| unset | set | only if it is an `lb://` URL |
| unset | unset | as the annotation says: `serviceId`, an `lb://` `baseUrl`, or `@LoadBalanced` |

These rules are implemented in one place, `CoApiDefinition.withOverrides()`, and covered by `ClientPropertiesTest`.

## Example

```yaml
coapi:
  mode: SYNC
  base-packages:
    - com.example.partner.clients
  clients:
    # Point a load-balanced client at a fixed host in this environment
    OrderClient:
      base-url: http://orders.staging.internal:8080
    # Supply the URL of a client declared as plain @CoApi
    PaymentClient:
      base-url: https://payments.example.com
      sync:
        interceptor:
          names:
            - paymentAuthInterceptor
```

## Without Spring Boot

The `coapi.clients.*` properties are a Spring Boot feature. In plain Spring, the same settings come from optional beans that you implement:

| Bean | Provides |
|------|----------|
| `ClientProperties` | `getBaseUri(name)`, `getLoadBalanced(name)` |
| `ReactiveClientProperties` | `getFilter(name)` |
| `SyncClientProperties` | `getInterceptor(name)` |

Each one is optional: if it is missing, the client keeps exactly what its annotation declares. With Spring Boot, `CoApiProperties` implements all three. `coapi.mode` is read from the `Environment` in both cases.
