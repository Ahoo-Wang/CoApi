---
title: 认证
description: 使用 HeaderSetFilter、BearerTokenFilter 和可自动刷新的 CachedExpirableTokenProvider 为 CoApi 客户端添加 Authorization 等请求头。
---

# 认证

CoApi 在 `me.ahoo.coapi.spring.client.reactive.auth` 中提供了基于请求头的响应式认证组件。它们都是普通的 `ExchangeFilterFunction`，挂载方式与其他[过滤器](./customization.md#单客户端过滤器与拦截器)相同。

| 类型 | 作用 |
|------|------|
| `HeaderSetFilter(headerName, valueProvider, valueMapper)` | 用异步获取的值设置请求头。**如果请求已带有该请求头，则不做修改。** |
| `BearerTokenFilter(tokenProvider)` | 设置 `Authorization: Bearer <token>` 的 `HeaderSetFilter` |
| `ExpirableToken(token, expireAt)` | 令牌及其过期时间（epoch 毫秒） |
| `ExpirableTokenProvider` | 提供 `Mono<ExpirableToken>`。实现它来获取令牌。 |
| `CachedExpirableTokenProvider(provider, refreshBeforeExpiry = 60s)` | 缓存令牌，并在令牌将在 `refreshBeforeExpiry` 内过期时获取新令牌 |
| `String.jwtToExpirableToken()` | 读取 JWT 的 `exp` 声明（需要 `com.auth0:java-jwt`） |

这些类用于响应式模式。同步模式见[下文](#同步模式)。

## 从令牌端点获取 Bearer 令牌

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

`CachedExpirableTokenProvider` 在并发请求之间共享同一个缓存令牌。它默认在过期前 60 秒刷新，以免令牌因请求延迟或客户端与服务端的时钟偏差，在发送途中过期而被拒绝。需要时可传入其他 `Duration`。整个有效期比该提前量还短的令牌，每次使用都会重新获取。

对于非 JWT 令牌，自行计算过期时间：`ExpirableToken(token, System.currentTimeMillis() + expiresInMillis)`。

## 其他请求头

`HeaderSetFilter` 适用于任意请求头，例如静态 API Key：

```kotlin
@Bean
fun apiKeyFilter(@Value("\${partner.api-key}") apiKey: String) =
    HeaderSetFilter("X-Api-Key", { Mono.just(apiKey) })
```

由于它会跳过已带有该请求头的请求，方法仍可以通过 `@RequestHeader("X-Api-Key")` 按次覆盖。

## 同步模式

CoApi 没有内置同步版本。请使用 `ClientHttpRequestInterceptor`，并在 `sync.interceptor` 下引用：

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

对于 OAuth2 客户端凭证模式，Spring Security 的 `OAuth2ClientHttpRequestInterceptor`（同步）和 `ServerOAuth2AuthorizedClientExchangeFilterFunction`（响应式）同样可以声明为 Bean，并以相同方式引用。
