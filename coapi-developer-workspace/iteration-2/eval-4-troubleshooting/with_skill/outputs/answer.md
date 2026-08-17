# Eval 4 — NoSuchBeanDefinitionException troubleshooting checklist (with_skill)

## Troubleshooting checklist: `NoSuchBeanDefinitionException` for a `@CoApi` interface

Work through these in order — they cover essentially every cause of this failure.

### 1. Is it actually an interface?

CoApi's scanner only accepts interfaces (`ApiClientScanner` rejects anything where `metadata.isInterface` is false). A `@CoApi`-annotated **class** is silently ignored, and the bean is never registered — which surfaces later as `NoSuchBeanDefinitionException`. Make it an `interface` with `@HttpExchange` methods.

### 2. Is the interface inside a scanned package?

The starter's `AutoCoApiRegistrar` scans only:

- the application's auto-configuration packages — i.e. the `@SpringBootApplication` root package and its sub-packages, and
- any packages listed in `coapi.base-packages`.

If your client lives in a different package tree or a separate JAR, pick one fix:

```kotlin
// Option A: explicit registration (most reliable, works across JARs)
@EnableCoApi(clients = [GitHubApiClient::class, UserClient::class])
@SpringBootApplication
class ConsumerApplication
```

```yaml
# Option B: extend auto-scanning
coapi:
  base-packages:
    - com.example.clients
    - com.shared.apis
```

Note: `@EnableCoApi` registers exactly the interfaces in `clients = [...]`; an empty `clients` array registers nothing.

### 3. Is the starter actually on the classpath (and not excluded)?

- You need `implementation("me.ahoo.coapi:coapi-spring-boot-starter")` — not just the `coapi-api` annotation module. Only the starter contributes the auto-scan registrar via `AutoConfiguration.imports`.
- Check you haven't excluded CoApi's auto-configuration (`spring.autoconfigure.exclude` or `@SpringBootApplication(exclude = ...)`) — that disables scanning entirely. (`@EnableCoApi(clients = [...])` from the `spring` module still works in that case.)

### 4. Is the annotation the right one, on the right target?

`@CoApi` must be directly on the client interface — it is **not** inherited from a super-interface. In the shared-contract pattern, the parent carries `@HttpExchange` methods, but the `@CoApi(serviceId = ...)` annotation goes on the consumer-side interface that extends it:

```kotlin
@HttpExchange("todo")
interface TodoApi { ... }          // shared contract — no @CoApi here

@CoApi(serviceId = "provider-service")
interface TodoClient : TodoApi     // @CoApi goes here
```

Also confirm you imported `me.ahoo.coapi.api.CoApi` and not some other annotation with the same simple name.

### 5. Quick diagnostics

- Enable debug logging for the registration phase (`logging.level.me.ahoo.coapi=DEBUG`) and look for your client in the registered definitions.
- In a test or actuator, list bean definitions of type `CoApiDefinition` — each registered client contributes one; if yours is missing, registration never happened (points back to items 1–4).
- If startup instead fails with `IllegalStateException: Duplicate CoApi name [...]`, that's a different problem (name collision, v2.2.0+), not a missing bean — fix by assigning a distinct `@CoApi(name = "...")`.

Most common cause in practice: the interface sits outside the `@SpringBootApplication` package — fix it with `@EnableCoApi(clients = [...])` or `coapi.base-packages`.
