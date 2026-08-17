# Eval 5 — Base URL precedence explanation (with_skill)

## CoApi Base URL Precedence

Base URL resolution happens in **two layers**: annotation parsing first, then per-client configuration override at HTTP client build time.

### Layer 1 — Annotation parsing (`CoApiDefinition`)

When the client interface is registered, `@CoApi` attributes are resolved in this order:

1. **`@CoApi(baseUrl = "...")` wins first.** If `baseUrl` is non-blank, `serviceId` is ignored entirely — avoid setting both on the same interface. Placeholders must resolve in the active environment (`resolveRequiredPlaceholders`); an unresolvable `${...}` fails startup, and `${name:default}` provides an inline fallback.
2. **`@CoApi(serviceId = "...")`** is used only when `baseUrl` is blank. It is treated as `lb://<serviceId>`, then normalized to `http://<serviceId>` with `loadBalanced = true`.
3. If both are blank, the definition's URL is blank and the client is expected to be configured externally (or use dynamic `URI`/`UriBuilderFactory` parameters).

An `lb://...` `baseUrl` (e.g. `@CoApi(baseUrl = "lb://order-service")`) follows the same normalization: stored as `http://order-service`, `loadBalanced = true`. The `@LoadBalanced` annotation also marks the definition load-balanced.

### Layer 2 — Per-client property override (`AbstractHttpClientFactoryBean`)

When the backing `WebClient`/`RestClient` is actually built:

- **`coapi.clients.<name>.base-url` (non-blank) overrides everything** from Layer 1.
- If it is blank/unset, the factory falls back to the parsed `CoApiDefinition.baseUrl`.

The `<name>` key is `@CoApi(name = "...")` if set, otherwise the interface simple name.

### Load-balanced interplay

`loadBalanced()` follows the same layering:

1. `coapi.clients.<name>.load-balanced` — explicit `true`/`false` wins.
2. If unset but `base-url` is non-blank → **forces direct (non-load-balanced) wiring**, since you pointed at a concrete endpoint.
3. Otherwise the annotation-derived value (`serviceId`, `lb://`, or `@LoadBalanced` → true).

### Example

```kotlin
@CoApi(serviceId = "user-service")
interface UserClient
```

```yaml
coapi:
  clients:
    UserClient:
      base-url: http://user-service.prod.internal:8080  # wins; load balancing off
      # or: load-balanced: false + base-url for a direct pinned URL
```

### Summary

| Priority | Source |
|---|---|
| 1 | `coapi.clients.<name>.base-url` (also disables LB unless `load-balanced: true`) |
| 2 | `@CoApi(baseUrl)` — including `lb://` form |
| 3 | `@CoApi(serviceId)` → `http://<serviceId>`, load-balanced |
| 4 | blank (external config or dynamic URI required) |

Note: the property override is applied at factory time only — it does not rewrite the parsed `CoApiDefinition`. Implementation lives in `spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiDefinition.kt` and `spring/src/main/kotlin/me/ahoo/coapi/spring/client/AbstractHttpClientFactoryBean.kt`.
