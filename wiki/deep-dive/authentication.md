---
title: Authentication
description: Adding Authorization and other headers to CoApi clients with HeaderSetFilter, BearerTokenFilter and the refreshing CachedExpirableTokenProvider.
---

# Authentication

CoApi ships reactive building blocks for header-based authentication in `me.ahoo.coapi.spring.client.reactive.auth`. They are ordinary `ExchangeFilterFunction`s. You attach them to clients like any other [filter](./customization.md#per-client-filters-and-interceptors).

| Type | Role |
|------|------|
| `HeaderSetFilter(headerName, valueProvider, valueMapper)` | Sets a header from an asynchronous value. **Leaves the request untouched if it already has that header.** |
| `BearerTokenFilter(tokenProvider)` | `HeaderSetFilter` for `Authorization: Bearer <token>` |
| `ExpirableToken(token, expireAt)` | A token and its expiry in epoch milliseconds |
| `ExpirableTokenProvider` | Supplies `Mono<ExpirableToken>`. Implement this to fetch tokens. |
| `CachedExpirableTokenProvider(provider, refreshBeforeExpiry = 60s)` | Caches the token and fetches a new one once it expires within `refreshBeforeExpiry` |
| `String.jwtToExpirableToken()` | Reads a JWT's `exp` claim (needs `com.auth0:java-jwt`) |

These classes are for reactive mode. For sync mode, see [below](#sync-mode).

## Bearer token from a token endpoint

```kotlin
class LoginTokenProvider(private val authClient: AuthClient) : ExpirableTokenProvider {
    override fun getToken(): Mono<ExpirableToken> =
        authClient.login().map { it.accessToken.jwtToExpirableToken() }
}

@Configuration
class AuthConfiguration {
    @Bean
    fun orderAuthFilter(authClient: AuthClient) =
        BearerTokenFilter(CachedExpirableTokenProvider(LoginTokenProvider(authClient)))
}
```

```yaml
coapi:
  clients:
    OrderClient:
      reactive:
        filter:
          names: [orderAuthFilter]
```

`CachedExpirableTokenProvider` shares one cached token across concurrent requests. It refreshes 60 seconds before expiry by default, so a token is not sent and then rejected in flight because of latency or clock skew. Pass a different `Duration` if needed. A token whose whole lifetime is shorter than the margin is fetched on every use.

For tokens that are not JWTs, compute the expiry yourself: `ExpirableToken(token, System.currentTimeMillis() + expiresInMillis)`.

## Other headers

`HeaderSetFilter` works for any header, for example a static API key:

```kotlin
@Bean
fun apiKeyFilter(@Value("\${partner.api-key}") apiKey: String) =
    HeaderSetFilter("X-Api-Key", { Mono.just(apiKey) })
```

Because it skips requests that already carry the header, a method can still pass `@RequestHeader("X-Api-Key")` to override it per call.

## Sync mode

There are no built-in sync equivalents. Use a `ClientHttpRequestInterceptor` and reference it under `sync.interceptor`:

```kotlin
@Bean
fun orderAuthInterceptor(tokens: TokenService) = ClientHttpRequestInterceptor { request, body, execution ->
    if (!request.headers.containsHeader(HttpHeaders.AUTHORIZATION)) {
        request.headers.setBearerAuth(tokens.currentToken())
    }
    execution.execute(request, body)
}
```

```yaml
coapi:
  clients:
    OrderClient:
      sync:
        interceptor:
          names: [orderAuthInterceptor]
```

For OAuth2 client credentials, Spring Security's `OAuth2ClientHttpRequestInterceptor` (sync) and `ServerOAuth2AuthorizedClientExchangeFilterFunction` (reactive) are beans you can reference the same way.
