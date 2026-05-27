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

- `baseUrl`: Direct base URL, including property placeholders such as `${github.url}`.
- `serviceId`: Service discovery ID. CoApi constructs `lb://<serviceId>`.
- `name`: Optional logical client name for bean names and per-client configuration.

Avoid setting both `baseUrl` and `serviceId` on the same client. Prefer `serviceId` for service
discovery and `baseUrl` for fixed external endpoints.

## Base URL Resolution

CoApi resolves the target URL in two layers:

1. `CoApiDefinition` resolves annotation data. `@CoApi(baseUrl = "...")` wins first; if it is blank,
   `@CoApi(serviceId = "...")` becomes `lb://<serviceId>`; otherwise the definition URL is blank.
2. `AbstractHttpClientFactoryBean.getBaseUrl()` checks `coapi.clients.<name>.base-url` first. If that
   property is blank, it falls back to `CoApiDefinition.baseUrl`.

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
`@LoadBalanced`.

Requirements:

- Add `org.springframework.cloud:spring-cloud-starter-loadbalancer`.
- Reactive clients use a `WebClient` filter such as `loadBalancerExchangeFilterFunction`.
- Sync clients use a `RestClient` interceptor such as `loadBalancerInterceptor`.

## Bean And Factory Shape

Useful repository names:

- `CoApiDefinition`: parsed annotation and configuration data.
- `CoApiFactoryBean`: creates the HTTP interface proxy.
- `WebClientFactoryBean`: creates reactive HTTP clients.
- `RestClientFactoryBean`: creates synchronous HTTP clients.
- `ReactiveHttpExchangeAdapterFactory` and `SyncHttpExchangeAdapterFactory`: create Spring HTTP exchange adapters.
- `AutoCoApiRegistrar` and `EnableCoApiRegistrar`: discover and register client beans.
- `ClientMode`: mode selection and classpath inference.

Bean naming conventions are usually `{name}.HttpClient` for the backing HTTP client and `{name}.CoApi`
for the proxy.

## Scope Boundaries

CoApi wires typed request-response clients. It does not own:

- WebSocket or SSE clients.
- Retry, circuit breaker, rate limiting, or timeout policy beyond what filters/interceptors configure.
- One-off HTTP calls that do not benefit from a typed interface.
- Non-Spring application wiring.

For those cases, recommend direct Spring `WebClient`, `RestClient`, `WebSocketClient`, filters,
interceptors, or Resilience4j.
