# CoApi Concepts

Load this reference when explaining CoApi architecture, choosing a configuration model, or editing the
CoApi repository itself.

## Contents

- [Domain Model](#domain-model)
- [Modules](#modules)
- [Client Definition](#client-definition)
- [Base URL Resolution](#base-url-resolution)
- [Client Mode](#client-mode)
- [Load Balancing](#load-balancing)
- [Bean And Factory Shape](#bean-and-factory-shape)
- [Scope Boundaries](#scope-boundaries)

## Domain Model

CoApi provides Spring Boot auto-configuration for Spring HTTP Interface clients. Users declare
interfaces with Spring `@HttpExchange` methods, mark client interfaces with `@CoApi`, and inject the
generated proxy as a Spring bean.

Current repository assumptions:

- Kotlin/JVM 17.
- Spring Boot 4.x and Spring Framework 7.x in the current mainline.
- Published artifacts use `me.ahoo.coapi:*`.
- The starter artifact is `me.ahoo.coapi:coapi-spring-boot-starter`.

## Modules

| Module | Responsibility |
| --- | --- |
| `api` | Public annotations such as `@CoApi` and `@LoadBalanced`. Public API changes require care. |
| `spring` | Core registrar, definition parsing, factory beans, and reactive/sync client SPI. |
| `spring-boot-starter` | Auto-configuration and `CoApiProperties`. |
| `example/*` | Provider/consumer examples for shared API contracts and client usage. |

## Client Definition

`@CoApi` belongs on an interface. CoApi relies on Java dynamic proxies, so classes are not valid client
targets.

Key annotation fields:

- `baseUrl`: Direct base URL, including property placeholders such as `${github.url}`. Since v2.2.0 placeholders are resolved with `resolveRequiredPlaceholders`: an unresolvable `${...}` fails startup; `${name:default}` provides an inline fallback.
- `serviceId`: Service discovery ID. CoApi first resolves it through the load-balanced URL path, then stores a normalized `http://<serviceId>` base URL with `loadBalanced=true`.
- `name`: Optional logical client name for bean names and per-client configuration. Defaults to the interface simple name and must be unique: duplicate names fail registration with the conflicting types listed (v2.2.0). The name is also the `coapi.clients.<name>.*` configuration key.

Avoid setting both `baseUrl` and `serviceId` on the same client. Prefer `serviceId` for service
discovery and `baseUrl` for fixed external endpoints.

## Base URL Resolution

CoApi resolves the target URL in two layers:

1. `CoApiDefinition` resolves annotation data. `@CoApi(baseUrl = "...")` wins first; if it is blank,
   `@CoApi(serviceId = "...")` is resolved through `lb://<serviceId>`. `toCoApiDefinition()` then
   normalizes `lb://...` to `http://...` and sets `loadBalanced=true`; otherwise the definition URL is blank.
2. `AbstractHttpClientFactoryBean.getBaseUrl()` checks `coapi.clients.<name>.base-url` first. If that
   property is blank, it falls back to `CoApiDefinition.baseUrl`. `loadBalanced()` follows the same
   layering: `coapi.clients.<name>.load-balanced` (true or false) wins; otherwise a non-blank
   `base-url` property forces non-load-balanced; otherwise the annotation-derived value.

Use this carefully when explaining precedence: per-client configuration can override the annotation at
factory time, but it does not change the parsed `CoApiDefinition`.

## Client Mode

`coapi.mode` selects the HTTP stack:

- `AUTO`: infer from the classpath. Reactive web classes lead to `WebClient`; otherwise CoApi uses `RestClient`.
- `REACTIVE`: force `WebClient`.
- `SYNC`: force `RestClient`.

Use explicit mode when classpath inference is surprising or when tests need deterministic wiring.

## Load Balancing

Load balancing is activated when the client uses `serviceId`, an `lb://` base URL, or explicit
`@LoadBalanced`. The parsed `CoApiDefinition.baseUrl` passed to client factories uses an `http://...`
URL; load-balancer behavior comes from `loadBalanced=true` plus the matching filter or interceptor.

Requirements:

- Add `org.springframework.cloud:spring-cloud-starter-loadbalancer`.
- Reactive clients use a `WebClient` filter such as `loadBalancerExchangeFilterFunction`.
- Sync clients use a `RestClient` interceptor; CoApi resolves it by the `BlockingLoadBalancerInterceptor`
  interface, so it works with or without Spring Cloud retry.
- Both factory beans dedupe existing Spring Cloud LB components (`Deferring*`, `Retry*` variants) on
  the builder before adding their own, so builders that already carry load balancing are not double-wired.

## Bean And Factory Shape

Useful repository names:

- `CoApiDefinition`: parsed annotation and configuration data.
- `CoApiFactoryBean`: creates the HTTP interface proxy.
- `WebClientFactoryBean`: creates reactive HTTP clients.
- `RestClientFactoryBean`: creates synchronous HTTP clients.
- `ReactiveHttpExchangeAdapterFactory` and `SyncHttpExchangeAdapterFactory`: create Spring HTTP exchange adapters.
- `AutoCoApiRegistrar` and `EnableCoApiRegistrar`: discover and register client beans.
- `ClientMode`: mode selection and classpath inference.

Bean naming conventions are `{name}.HttpClient` for the backing HTTP client and `{name}.CoApi`
for the proxy. `CoApiFactoryBean` resolves the `HttpExchangeAdapterFactory` like Spring's by-type
lookup: the unique candidate wins, then the `@Primary` one, then the bean registered under the
standard name `CoApi.HttpExchangeAdapterFactory`.

## Scope Boundaries

CoApi wires typed request-response clients. It does not own:

- WebSocket or SSE clients.
- Retry, circuit breaker, rate limiting, or timeout policy beyond what filters/interceptors configure.
- One-off HTTP calls that do not benefit from a typed interface.
- Non-Spring application wiring.

For those cases, recommend direct Spring `WebClient`, `RestClient`, `WebSocketClient`, filters,
interceptors, or Resilience4j.
