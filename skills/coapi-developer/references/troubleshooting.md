# CoApi Troubleshooting

Load this reference when diagnosing startup failures, missing clients, wrong HTTP stack selection, load
balancing issues, or base URL resolution problems.

## Contents

- [Client Bean Not Found](#client-bean-not-found)
- [Duplicate Client Name](#duplicate-client-name)
- [Load Balancing Not Working](#load-balancing-not-working)
- [Wrong Client Mode](#wrong-client-mode)
- [Base URL Not Resolving](#base-url-not-resolving)
- [Unsupported Concern](#unsupported-concern)

## Client Bean Not Found

Typical symptom: `NoSuchBeanDefinitionException` for a CoApi client interface.

Check:

1. The target is an interface, not a class.
2. The interface has `@CoApi` or is explicitly registered through `@EnableCoApi`.
3. The package is covered by component scanning or `coapi.base-packages`.
4. The application imports the starter dependency, not only the annotation API.
5. If the client lives in another JAR, use `@EnableCoApi(clients = [...])`.

Since v2.2.0, duplicate client names no longer manifest as a missing bean: registration fails fast
with `IllegalStateException` instead (see [Duplicate Client Name](#duplicate-client-name)).

## Duplicate Client Name

Typical symptom (v2.2.0+): startup fails with
`IllegalStateException: Duplicate CoApi name [...]` listing the conflicting types.

Cause: two `@CoApi` interfaces resolve to the same client name — identical custom `name`, or
identical interface simple names in different packages. The name is also the
`coapi.clients.<name>.*` configuration key, so it must be unique.

Fix:

1. Assign a distinct `@CoApi(name = "...")` to one of the conflicting interfaces.
2. Rename one of the interfaces when both simple names collide and no custom name is set.
3. Remove the duplicate if it is a dead interface.

## Load Balancing Not Working

Typical symptoms: direct host calls, unresolved service names, or connection refused.

Check:

1. `org.springframework.cloud:spring-cloud-starter-loadbalancer` is on the classpath.
2. The client uses `serviceId`, an `lb://` base URL, or `@LoadBalanced`.
3. Reactive mode has a load-balancer `WebClient` filter available.
4. Sync mode has a load-balancer `RestClient` interceptor available.
5. Per-client filter/interceptor names match actual Spring bean names.

## Wrong Client Mode

Typical symptom: CoApi creates `RestClient` when the user expected `WebClient`, or the reverse.

Check:

1. `coapi.mode` is not forcing the unexpected mode.
2. `AUTO` mode sees the expected classpath. Reactive web classes cause reactive mode inference.
3. Tests set mode explicitly when both reactive and sync dependencies are present.
4. The method return type is compatible with the selected adapter.

An invalid `coapi.mode` value fails startup with the offending value and the valid options
(`REACTIVE, SYNC, AUTO`) listed (v2.2.0).

Prefer explicit `REACTIVE` or `SYNC` for deterministic tests and production deployments that should not
change behavior when dependencies move.

## Base URL Not Resolving

Typical symptoms: startup failure, unresolved placeholders, or requests going to the wrong target.

Check:

1. Placeholder properties such as `${github.url}` exist in the active environment. Since v2.2.0 an
   unresolvable placeholder fails startup with `Could not resolve placeholder ...` instead of leaking
   the literal `${...}` into the base URL; use `${name:default}` for an inline fallback.
2. The client does not set both `baseUrl` and `serviceId`.
3. `serviceId` and `lb://...` targets are expected to appear as `http://...` in the parsed base URL, with `loadBalanced=true`.
4. `coapi.clients.<name>.base-url` uses the correct client name — the `@CoApi` `name` attribute when
   set, otherwise the interface simple name — and may override the annotation URL.
5. Empty `@CoApi` clients have an external `base-url` configured, or use absolute URIs per request.

## Unsupported Concern

If the user asks for streaming protocols, retries, circuit breakers, or one-off HTTP calls, explain the
boundary:

- WebSocket/SSE: use Spring `WebSocketClient` or `WebClient` streaming directly.
- Retry/circuit breaker: use filters/interceptors plus Resilience4j or a similar library.
- One-off calls: use `WebClient` or `RestClient` directly.
- Non-Spring runtime: use Spring `HttpServiceProxyFactory` manually or another HTTP client.
