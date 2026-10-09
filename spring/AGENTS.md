# AGENTS.md — spring Module

## Build & Run

```bash
./gradlew :spring:build
./gradlew :spring:test
./gradlew :spring:test --tests "me.ahoo.coapi.spring.CoApiDefinitionTest"
```

## Module Role

Core Spring integration. Contains registrar, factory beans, client implementations (reactive + sync), and auth filters.

## Key Directories

```
spring/src/main/kotlin/me/ahoo/coapi/spring/
├── CoApiDefinition.kt          # Parsed @CoApi metadata; normalize()/withOverrides() own the URL + LB rules
├── CoApiFactoryBean.kt         # Creates JDK proxy via HttpServiceProxyFactory
├── CoApiRegistrar.kt           # Registers all mode-dependent beans: HttpExchangeAdapterFactory + client/proxy per @CoApi
├── AbstractCoApiRegistrar.kt   # Template: mode inference; subclasses supply CoApiDefinitions
├── EnableCoApi.kt              # @EnableCoApi annotation
├── EnableCoApiRegistrar.kt     # Manual mode registrar
├── ClientMode.kt               # REACTIVE/SYNC/AUTO enum
├── HttpExchangeAdapterFactory.kt  # SPI interface
└── client/
    ├── AbstractHttpClientFactoryBean.kt  # Shared factory plumbing: effectiveDefinition(), component/customizer lookup
    ├── ClientProperties.kt               # Endpoint overrides (baseUrl/loadBalanced) + resolve(); optional, Empty fallback
    ├── ComponentDefinition.kt            # Filter/interceptor references by bean name and type
    ├── HttpClientBuilderCustomizer.kt    # Base SPI for customizers (receives the effective definition)
    ├── reactive/                         # WebClient stack
    │   ├── WebClientFactoryBean.kt
    │   ├── LoadBalancedWebClientBuilderCustomizer.kt  # internal, only for load-balanced clients
    │   ├── ReactiveClientProperties.kt   # Per-client ExchangeFilterFunctions
    │   ├── ReactiveHttpExchangeAdapterFactory.kt
    │   ├── WebClientBuilderCustomizer.kt
    │   └── auth/                         # BearerTokenFilter, CachedExpirableTokenProvider
    └── sync/                             # RestClient stack
        ├── RestClientFactoryBean.kt
        ├── LoadBalancedRestClientBuilderCustomizer.kt # internal, only for load-balanced clients
        ├── SyncClientProperties.kt       # Per-client ClientHttpRequestInterceptors
        ├── SyncHttpExchangeAdapterFactory.kt
        └── RestClientBuilderCustomizer.kt
```

## Feature Variants (build.gradle.kts)

- `reactiveSupport`: spring-boot-webclient
- `lbSupport`: spring-cloud-commons
- `jwtSupport`: java-jwt

## Testing

- Use `ApplicationContextRunner` for Spring context tests
- Assertions: `me.ahoo.test.asserts.assert`
- Mocking: MockK

## Boundaries

- ✅ Extend client SPI with new customizer interfaces
- ✅ Add new auth filter implementations
- ⚠️ Changes to `CoApiDefinition` parsing affect all consumers
- 🚫 Do not modify `CoApiRegistrar` bean registration logic without updating boot-starter tests
