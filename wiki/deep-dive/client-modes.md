---
title: Client Modes
description: How CoApi chooses between WebClient (reactive) and RestClient (sync), and what each mode means for return types and dependencies.
---

# Client Modes

CoApi backs every client with either a `WebClient` or a `RestClient`. The **client mode** decides which one, for all clients in the application context.

| Mode | HTTP client | Adapter | Builder bean required |
|------|-------------|---------|-----------------------|
| `REACTIVE` | `WebClient` | `WebClientAdapter` | `WebClient.Builder` |
| `SYNC` | `RestClient` | `RestClientAdapter` | `RestClient.Builder` |
| `AUTO` (default) | resolved to one of the above at startup | | |

```yaml
coapi:
  mode: SYNC   # REACTIVE | SYNC | AUTO, case-insensitive
```

## How `AUTO` decides

`AUTO` checks whether Spring WebFlux is on the classpath (the class `org.springframework.web.reactive.HandlerResult`):

- present: `REACTIVE`
- absent: `SYNC`

This is a classpath check, not Spring Boot's web application type. A Spring MVC application that has WebFlux on the classpath (for example, to use `WebClient` elsewhere) resolves to `REACTIVE`. Set the mode explicitly when that is not what you want. Explicit modes also make tests deterministic.

## Return types

The mode determines which Spring adapter executes your interface methods, so it constrains return types:

- **Reactive mode**: declare `Mono<T>`, `Flux<T>` or `Mono<ResponseEntity<T>>`. Spring's `WebClientAdapter` also accepts blocking return types (`T`, `List<T>`), but those block the calling thread. Avoid them on event-loop threads.
- **Sync mode**: declare plain types: `T`, `List<T>`, `ResponseEntity<T>`, `Unit`/`void`. `RestClientAdapter` cannot return `Mono`/`Flux`.

If you might switch modes later, keep return types compatible with both, or keep separate interfaces.

## The builder bean

In both modes CoApi asks the context for **one** builder bean, configures it for the client, and calls `build()`:

- Spring Boot provides prototype-scoped builders through `spring-boot-starter-webclient` / `spring-boot-starter-restclient`. Boot's own builder customizations (codecs, message converters, observation) therefore apply to CoApi clients too.
- If several builder beans exist, mark one `@Primary`.
- If you define the builder yourself, make it `@Scope("prototype")`. Each client mutates the builder it receives.

See [Customization](./customization.md) for what CoApi applies to the builder and in which order.

## Mixing modes

There is one mode per application context. To call some APIs reactively and others synchronously in the same application, choose `REACTIVE`: blocking return types still work on the `WebClient` adapter, as described above. The alternative is to use a separate `WebClient` or `RestClient` for the odd ones out.
