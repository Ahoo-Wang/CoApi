---
title: Registering Clients
description: How CoApi finds client interfaces, through Spring Boot classpath scanning, coapi.base-packages, @EnableCoApi, or CoApiDefinition beans, and how to use it without Spring Boot.
---

# Registering Clients

A `@CoApi` interface becomes a bean only once a registrar finds it. There are four ways to make sure that happens. You can combine them.

| Way | Needs Spring Boot | Use it for |
|-----|-------------------|------------|
| Classpath scanning (default) | yes | Clients in your application's packages |
| `coapi.base-packages` | yes | Clients in other packages or JARs, by package |
| `@EnableCoApi(clients = [...])` | no | Clients listed explicitly, in any package |
| `CoApiDefinition` beans | yes | Interfaces you cannot annotate, or definitions built in code |

## Classpath scanning

With the starter on the classpath, `CoApiAutoConfiguration` scans the **auto-configuration packages** (the package of your `@SpringBootApplication` class and everything below it) for interfaces annotated with `@CoApi`. Classes are ignored.

Scanning is on unless `coapi.enabled=false`.

## `coapi.base-packages`

Adds packages to scan, typically for clients shipped in a library JAR:

```yaml
coapi:
  base-packages:
    - com.example.order.client
    - com.example.payment.client
```

A comma-separated string (`coapi.base-packages=com.a,com.b`) works too.

## `@EnableCoApi`

Registers exactly the listed interfaces:

```kotlin
@EnableCoApi(clients = [TodoClient::class, GitHubApiClient::class])
@SpringBootApplication
class ConsumerApplication
```

`@EnableCoApi` lives in `coapi-spring` and works without Spring Boot. It does not depend on `coapi.enabled`.

Registering the same interface by more than one way is harmless. The first registration wins, and later ones are skipped with a `WARN` log line.

## `CoApiDefinition` beans

When you cannot put `@CoApi` on an interface (it comes from a third party, or the definition is computed), declare a `CoApiDefinition` bean instead:

```kotlin
@Configuration
class OrderApiConfiguration {
    companion object {
        @JvmStatic
        @Bean
        fun orderApiDefinition(): CoApiDefinition = CoApiDefinition(
            name = "OrderApi",
            apiType = OrderApi::class.java,
            baseUrl = "lb://\${order.service-id}",
            loadBalanced = false, // lb:// already enables load balancing
        )
    }
}
```

Rules:

- The `@Bean` method must be **static** (Kotlin: `@JvmStatic` in a `companion object`). CoApi reads these beans before bean post-processing. A non-static method would create its configuration class too early, so startup fails instead.
- Placeholders and `lb://` in `baseUrl` are resolved just like on the annotation.
- Names must be unique across scanned, listed and definition clients.
- Only Spring Boot auto-configuration collects these beans. `@EnableCoApi` alone does not.

## Without Spring Boot

Use `coapi-spring` and `@EnableCoApi` on a configuration class, and provide what Boot would otherwise provide:

```kotlin
@Configuration
@EnableCoApi(clients = [GitHubApiClient::class])
class ClientConfiguration {
    @Bean
    @Scope("prototype")
    fun restClientBuilder(): RestClient.Builder = RestClient.builder()
}
```

- A `WebClient.Builder` or `RestClient.Builder` bean for the [client mode](./client-modes.md). Make it prototype-scoped, because CoApi configures the builder it receives.
- `coapi.mode` is read from the `Environment`. Set it explicitly, because `AUTO` may pick a mode whose builder you did not define.
- Optionally, `ClientProperties` / `ReactiveClientProperties` / `SyncClientProperties` beans for per-client overrides. See [Configuration](../getting-started/configuration.md#without-spring-boot).
