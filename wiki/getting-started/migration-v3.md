---
title: Migrating to 3.0
description: Breaking changes in CoApi 3.0.0 and how to migrate from 2.x. YAML configuration is unchanged; only the Kotlin/Java extension SPI and CoApiDefinition beans are affected.
---

# Migrating to 3.0

CoApi 3.0.0 restructures the client SPI so that each type has one responsibility. **Applications that only use `@CoApi`, `@EnableCoApi` and `coapi.*` YAML properties need no changes**: every configuration key keeps its name and meaning.

You are affected only if you:

- implement or reference `ClientProperties`, `FilterDefinition` or `InterceptorDefinition` in code;
- subclass or reference `AbstractWebClientFactoryBean`, `AbstractRestClientFactoryBean` or `AbstractHttpClientFactoryBean`;
- declare `CoApiDefinition` beans.

## Client properties SPI

`ClientProperties` keeps only the mode-neutral endpoint overrides. Filters and interceptors moved to role interfaces in the reactive and sync packages, and the two definition types were unified into the generic `ComponentDefinition<T>`.

| 2.x | 3.0 |
|-----|-----|
| `ClientProperties.getBaseUri` / `getLoadBalanced` | unchanged, plus `resolve(definition)` |
| `ClientProperties.getFilter(name): FilterDefinition` | `ReactiveClientProperties.getFilter(name): ComponentDefinition<ExchangeFilterFunction>` |
| `ClientProperties.getInterceptor(name): InterceptorDefinition` | `SyncClientProperties.getInterceptor(name): ComponentDefinition<ClientHttpRequestInterceptor>` |
| `ClientProperties.FilterDefinition` / `InterceptorDefinition` | `me.ahoo.coapi.spring.client.ComponentDefinition<T>` |

```kotlin
// 2.x
class MyClientProperties : ClientProperties {
    override fun getBaseUri(coApiName: String) = ""
    override fun getLoadBalanced(coApiName: String): Boolean? = null
    override fun getFilter(coApiName: String) = ClientProperties.FilterDefinition(names = listOf("authFilter"))
    override fun getInterceptor(coApiName: String) = ClientProperties.InterceptorDefinition()
}

// 3.0 — implement only the roles you need; each one is an optional bean
class MyClientProperties : ClientProperties, ReactiveClientProperties {
    override fun getBaseUri(coApiName: String) = ""
    override fun getLoadBalanced(coApiName: String): Boolean? = null
    override fun getFilter(coApiName: String) = ComponentDefinition<ExchangeFilterFunction>(names = listOf("authFilter"))
}
```

Without Spring Boot, every role is optional and falls back to its `Empty` implementation. With Spring Boot, `CoApiProperties` implements all three.

## Factory beans

`AbstractWebClientFactoryBean` and `AbstractRestClientFactoryBean` were removed. `WebClientFactoryBean` and `RestClientFactoryBean` are now final, and the load-balancing customizers are internal. `AbstractHttpClientFactoryBean` is generic, and its property getters (`getBaseUrl()`, `loadBalanced()`, `getBaseUrlFromProperties()`, `getLoadBalancedFromProperties()`) were replaced by `effectiveDefinition()`.

Move logic from a factory bean subclass into a customizer bean:

```kotlin
@Component
class TimeoutCustomizer : WebClientBuilderCustomizer {
    override fun customize(coApiDefinition: CoApiDefinition, builder: WebClient.Builder) {
        // coApiDefinition is the *effective* definition: coapi.clients.<name>.* overrides already applied
    }
}
```

Customizers now receive the **effective** definition. In 2.x they received the annotation definition, so `coApiDefinition.baseUrl` and `coApiDefinition.loadBalanced` could differ from what the client actually used.

## CoApiDefinition beans

A `CoApiDefinition` bean declared by a **non-static** `@Bean` method now fails startup (it logged a warning in 2.3.0). Such a method forced its configuration class to be created before bean post-processing, silently skipping `@Autowired`/`@Value` injection and `@Bean` proxying. Declare the method static, and let CoApi resolve placeholders and `lb://` in `baseUrl`, as it does for the annotation:

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
            loadBalanced = false, // lb:// enables load balancing
        )
    }
}
```

## New fail-fast checks

- A client that resolves to load balanced without Spring Cloud LoadBalancer on the classpath fails with a message naming the client and the fix, instead of a `NoClassDefFoundError`.

## Related Pages

- [Configuration Reference](./configuration.md)
- [Customization & Extensibility](../deep-dive/customization.md)
- [Load Balancing](../deep-dive/load-balancing.md)
- [Auto-Configuration](../deep-dive/auto-configuration.md)
