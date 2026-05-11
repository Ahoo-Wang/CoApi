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
├── CoApiDefinition.kt          # Parsed @CoApi metadata
├── CoApiFactoryBean.kt         # Creates JDK proxy via HttpServiceProxyFactory
├── CoApiRegistrar.kt           # Registers beans per @CoApi interface
├── AbstractCoApiRegistrar.kt   # Template: mode inference + HttpExchangeAdapterFactory registration
├── EnableCoApi.kt              # @EnableCoApi annotation
├── EnableCoApiRegistrar.kt     # Manual mode registrar
├── ClientMode.kt               # REACTIVE/SYNC/AUTO enum
├── HttpExchangeAdapterFactory.kt  # SPI interface
└── client/
    ├── AbstractHttpClientFactoryBean.kt  # Base: baseUrl + loadBalanced resolution
    ├── ClientProperties.kt               # Filter/interceptor definition interface
    ├── HttpClientBuilderCustomizer.kt    # Base SPI for customizers
    ├── reactive/                         # WebClient stack
    │   ├── WebClientFactoryBean.kt
    │   ├── AbstractWebClientFactoryBean.kt
    │   ├── ReactiveHttpExchangeAdapterFactory.kt
    │   ├── WebClientBuilderCustomizer.kt
    │   └── auth/                         # BearerTokenFilter, CachedExpirableTokenProvider
    └── sync/                             # RestClient stack
        ├── RestClientFactoryBean.kt
        ├── AbstractRestClientFactoryBean.kt
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
