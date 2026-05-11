---
title: Authentication
description: Deep dive into CoApi's authentication mechanisms, token management, and security filters
---

# Authentication

CoApi provides a robust authentication system that integrates seamlessly with Spring WebFlux reactive programming model. The authentication framework supports token-based authentication with automatic caching and expiry management, ensuring secure and efficient communication with external APIs while maintaining type safety and reactive principles.

## Overview

The authentication system in CoApi is designed to handle the common requirements of modern API authentication, particularly JWT (JSON Web Token) based authentication with Bearer tokens. The framework provides reusable components that can be easily configured and composed to meet various authentication scenarios, from simple static tokens to complex dynamic token providers with automatic refresh capabilities.

The authentication layer leverages Spring WebFlux's reactive programming model throughout, ensuring non-blocking operation and efficient resource utilization. The architecture follows a modular design with clear separation of concerns between token providers, header mappers, and request filters.

## At-a-Glance

| Component | Responsibility | Key Features | Source |
|----------|---------------|---------------|--------|
| `HeaderSetFilter` | Generic header injection on requests | Configurable header names, value providers, mappers | [HeaderSetFilter.kt:22](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/auth/HeaderSetFilter.kt#L22) |
| `BearerTokenFilter` | Authorization header injection | Bearer token prefix, token provider integration | [BearerTokenFilter.kt:18](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/auth/BearerTokenFilter.kt#L18) |
| `ExpirableToken` | Token with expiration information | JWT expiry detection, time-based validation | [ExpirableToken.kt:19](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/auth/ExpirableTokenProvider.kt#L19) |
| `ExpirableTokenProvider` | Token provider interface | Reactive token fetching, header value mapping | [ExpirableTokenProvider.kt:33](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/auth/ExpirableTokenProvider.kt#L33) |
| `CachedExpirableTokenProvider` | Cached token provider | Reactive caching, automatic expiry invalidation | [CachedExpirableTokenProvider.kt:19](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/auth/CachedExpirableTokenProvider.kt#L19) |

## Class Hierarchy

The authentication framework follows a clean inheritance hierarchy with clear responsibilities:

```mermaid
classDiagram
    class HeaderSetFilter {
        +String headerName
        +HeaderValueProvider headerValueProvider
        +HeaderValueMapper headerValueMapper
        +filter(ClientRequest, ExchangeFunction)
    }
    class ExchangeFilterFunction {
        <<interface>>
        +filter(ClientRequest, ExchangeFunction)
    }
    class HeaderValueProvider {
        <<fun interface>>
        +getHeaderValue(): Mono~String~
    }
    class HeaderValueMapper {
        <<fun interface>>
        +map(String): String
    }
    class BearerTokenFilter {
        -ExpirableTokenProvider tokenProvider
        +filter(ClientRequest, ExchangeFunction)
    }
    class BearerHeaderValueMapper {
        <<object>>
        +map(String): String
        +String.withBearerPrefix()
    }
    class ExpirableToken {
        +String token
        +Long expireAt
        +Boolean isExpired
        +String.jwtToExpirableToken()
    }
    class ExpirableTokenProvider {
        <<interface>>
        +getToken(): Mono~ExpirableToken~
        +getHeaderValue(): Mono~String~
    }
    class CachedExpirableTokenProvider {
        +ExpirableTokenProvider delegate
        +Mono~ExpirableToken~ tokenCache
        +getToken(): Mono~ExpirableToken~
    }
    HeaderSetFilter --> ExchangeFilterFunction : implements
    HeaderSetFilter --> HeaderValueProvider : uses
    HeaderSetFilter --> HeaderValueMapper : uses
    BearerTokenFilter --> HeaderSetFilter : extends
    BearerTokenFilter --> ExpirableTokenProvider : uses
    BearerHeaderValueMapper --> HeaderValueMapper : implements
    ExpirableTokenProvider --> HeaderValueProvider : extends
    CachedExpirableTokenProvider --> ExpirableTokenProvider : implements
```

## Token Caching Flow

The reactive caching mechanism ensures efficient token management while maintaining thread safety and automatic invalidation:

```mermaid
sequenceDiagram
    participant C as CachedExpirableTokenProvider
    participant D as Delegate Provider
    participant T as Mono
    participant R as Reactive Cache
    participant U as User Request
    participant E as Expiration Check
    autonumber
    U->>C: getToken
    C->>T: tokenCache
    alt Cache Empty
        T->>D: getToken
        D->>D: Fetch token
        D->>T: Return ExpirableToken
        T->>R: cacheInvalidateIf
        R->>C: Return cached Mono
        T->>U: Return cached token
    else Cache Valid
        T->>U: Return cached token
    else Cache Expired
        T->>E: Check expiration
        E->>R: cacheInvalidateIf
        R->>T: Force refresh
        T->>D: getToken
        D->>D: Fetch fresh token
        D->>T: Return ExpirableToken
        T->>R: Update cache
        T->>U: Return fresh token
    end
    note over C,C: Reactive caching
```

## Request Authentication Flow

The complete authentication flow demonstrates how Bearer tokens are automatically added to HTTP requests:

```mermaid
sequenceDiagram
    participant U as User Application
    participant R as WebClient
    participant F as BearerTokenFilter
    participant T as CachedExpirableTokenProvider
    participant C as Cache
    participant P as ExpirableToken
    autonumber
    U->>R: makeRequest
    R->>F: filter request
    alt Authorization Header Exists
        F->>R: Skip
        R->>U: Return response
    else No Authorization Header
        F->>T: getHeaderValue
        alt Token Cached
            T->>C: Get cached token
            C->>T: Return ExpirableToken
            T->>F: Return token
        else Token Expired
            T->>P: getToken
            P->>P: Check expired
            alt Expired
                P->>C: Force refresh
                C->>T: Invalidate
                T->>P: fetchNewToken
                P->>P: return new token
            else Valid
                P->>T: Return token
            end
            T->>F: Return token
        end
        F->>F: Bearer prefix
        F->>F: Authorization header
        F->>R: Continue
        R->>U: Return response
    end
```

## JWT Expiry Check State Diagram

The token management system handles JWT expiry checks through a state machine approach:

```mermaid
stateDiagram-v2
    [*] --> NotRequested
    NotRequested --> Fetching : getToken called
    Fetching --> ValidToken : Token received, not expired
    Fetching --> InvalidToken : Token received, expired
    Fetching --> Error : Token fetch failed
    ValidToken --> Cached : Token cached
    Cached --> ValidCheck : Check if still valid
    ValidCheck --> ValidToken : Token still valid
    ValidCheck --> Expired : Token expired
    Expired --> Fetching : Auto-refresh triggered
    InvalidToken --> Fetching : Force refresh
    Error --> Fetching : Retry after delay
    [*] --> ValidToken : Initial token fetch
    ValidToken --> [*] : Application shutdown
```

## Core Components

### HeaderSetFilter

The `HeaderSetFilter` is a generic reactive filter that can set any header on HTTP requests. It follows the Spring WebFlux `ExchangeFilterFunction` interface and provides a flexible way to inject headers into requests.

```kotlin
open class HeaderSetFilter(
    private val headerName: String,
    private val headerValueProvider: HeaderValueProvider,
    private val headerValueMapper: HeaderValueMapper = HeaderValueMapper.IDENTITY
) : ExchangeFilterFunction {
    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        if (request.headers().containsHeader(headerName)) {
            return next.exchange(request)
        }
        return headerValueProvider.getHeaderValue()
            .map { headerValue ->
                ClientRequest.from(request)
                    .headers { headers ->
                        headers[headerName] = headerValueMapper.map(headerValue)
                    }
                    .build()
            }
            .flatMap { next.exchange(it) }
    }
}
```

The filter first checks if the header already exists in the request to avoid overwriting existing values. If the header is not present, it retrieves the header value from the provider, maps it using the configured mapper, and sets it on the request before proceeding to the next exchange function.

### BearerTokenFilter

The `BearerTokenFilter` is a specialized version of `HeaderSetFilter` that specifically handles Bearer token authentication. It extends the base filter with pre-configured values for the `Authorization` header and the Bearer token prefix.

```kotlin
class BearerTokenFilter(tokenProvider: ExpirableTokenProvider) :
    HeaderSetFilter(
        headerName = HttpHeaders.AUTHORIZATION,
        headerValueProvider = tokenProvider,
        headerValueMapper = BearerHeaderValueMapper
    )
```

The `BearerHeaderValueMapper` ensures that all token values are prefixed with "Bearer " according to the OAuth 2.0 Bearer Token specification.

### ExpirableToken

The `ExpirableToken` data class wraps a token string with its expiration timestamp, providing convenient methods for checking if the token has expired.

```kotlin
data class ExpirableToken(val token: String, val expireAt: Long) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expireAt

    companion object {
        private val jwtParser = JWT()
        fun String.jwtToExpirableToken(): ExpirableToken {
            val decodedJWT = jwtParser.decodeJwt(this)
            val expiresAt = checkNotNull(decodedJWT.expiresAt)
            return ExpirableToken(this, expiresAt.time)
        }
    }
}
```

The companion object provides a convenient extension function to convert JWT strings to `ExpirableToken` instances by decoding the JWT and extracting the expiration timestamp.

### CachedExpirableTokenProvider

The `CachedExpirableTokenProvider` implements reactive caching using Project Reactor's `Mono.cacheInvalidateIf` operator. This provides thread-safe caching with automatic invalidation when tokens expire.

```kotlin
class CachedExpirableTokenProvider(tokenProvider: ExpirableTokenProvider) : ExpirableTokenProvider {
    private val tokenCache: Mono<ExpirableToken> = tokenProvider.getToken()
        .cacheInvalidateIf {
            log.debug { "CacheInvalidateIf - isExpired:${it.isExpired}" }
            it.isExpired
        }

    override fun getToken(): Mono<ExpirableToken> {
        return tokenCache
    }
}
```

The cache automatically invalidates and refreshes tokens when they expire, ensuring that always-valid tokens are used without manual intervention.

## Configuration Examples

### Basic Bearer Token Authentication

```kotlin
@Configuration
class AuthenticationConfig {
    
    @Bean
    fun tokenProvider(): ExpirableTokenProvider {
        return object : ExpirableTokenProvider {
            override fun getToken(): Mono<ExpirableToken> {
                return Mono.just(
                    ExpirableToken(
                        token = "your.jwt.token",
                        expireAt = System.currentTimeMillis() + 3600000 // 1 hour
                    )
                )
            }
        }
    }
    
    @Bean
    fun authenticationFilter(): BearerTokenFilter {
        return BearerTokenFilter(tokenProvider())
    }
}
```

### Dynamic Token Provider with JWT Decoding

```kotlin
@Configuration
class DynamicAuthenticationConfig {
    
    @Bean
    fun jwtTokenProvider(): ExpirableTokenProvider {
        return object : ExpirableTokenProvider {
            override fun getToken(): Mono<ExpirableToken> {
                return Mono.fromCallable {
                    // Fetch token from external source (e.g., OAuth service)
                    val jwtToken = fetchTokenFromAuthService()
                    jwtToken.jwtToExpirableToken()
                }
            }
            
            private fun fetchTokenFromAuthService(): String {
                // Implementation to fetch JWT from authentication service
                return "dynamic.jwt.token"
            }
        }
    }
    
    @Bean
    fun cachedTokenProvider(): ExpirableTokenProvider {
        return CachedExpirableTokenProvider(jwtTokenProvider())
    }
}
```

### WebClient Integration

```kotlin
@Configuration
class WebClientConfig {
    
    @Bean
    fun webClient(builder: WebClient.Builder): WebClient {
        return builder
            .filter(authenticationFilter())
            .build()
    }
    
    @Bean
    fun authenticationFilter(): ExchangeFilterFunction {
        return BearerTokenFilter(cachedTokenProvider())
    }
}
```

## Cross-References

- [Getting Started](../getting-started/index.md) - Introduction to CoApi basics
- [Client Modes](./client-modes.md) - Understanding reactive vs sync operation
- [Spring Boot Integration](.md) - Spring Boot specific patterns
- [Configuration Reference](../getting-started/configuration.md) - Complete configuration guide
- [Annotations](./annotations.md) - Annotation-based configuration

## References

### Source Files

- [HeaderSetFilter.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/auth/HeaderSetFilter.kt) - Generic header injection filter
- [BearerTokenFilter.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/auth/BearerTokenFilter.kt) - Bearer token authentication filter
- [ExpirableToken.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/auth/ExpirableTokenProvider.kt) - Token with expiration support
- [CachedExpirableTokenProvider.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/auth/CachedExpirableTokenProvider.kt) - Reactive caching implementation
- [BearerHeaderValueMapper.kt](https://github.com/Ahoo-Wang/CoApi/blob/main/spring/src/main/kotlin/me/ahoo/coapi/spring/client/reactive/auth/BearerTokenFilter.kt) - Bearer token prefix mapping

### Related Pages

- [Client Modes](./client-modes.md) - Understanding reactive vs sync operation
- [Configuration Reference](../getting-started/configuration.md) - Complete configuration guide
- [Spring Boot Integration](.md) - Spring Boot specific patterns
- [Load Balancing](./load-balancing.md) - Load balancing integration
- [Architecture Overview](./architecture.md) - System architecture and design
