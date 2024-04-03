package me.ahoo.coapi.spring.client.reactive.auth

import me.ahoo.coapi.spring.client.reactive.auth.ExpirableToken.Companion.jwtToExpirableToken
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.util.*

class CachedExpirableTokenProviderTest {

    @Test
    fun getBearerToken() {
        val cachedExpirableTokenProvider = CachedExpirableTokenProvider(MockBearerTokenProvider)
        cachedExpirableTokenProvider.getToken()
            .test()
            .consumeNextWith {
                // 仅当缓存当前已填充时才会评估
                assertThat(it, equalTo(MockBearerTokenProvider.expiredToken))
            }.verifyComplete()

        cachedExpirableTokenProvider.getToken()
            .test()
            .consumeNextWith {
                assertThat(it, equalTo(MockBearerTokenProvider.notExpiredToken))
            }.verifyComplete()
        cachedExpirableTokenProvider.getToken()
            .test()
            .consumeNextWith {
                assertThat(it, equalTo(MockBearerTokenProvider.notExpiredToken))
            }.verifyComplete()
        cachedExpirableTokenProvider.getToken()
            .test()
            .consumeNextWith {
                assertThat(it, equalTo(MockBearerTokenProvider.notExpiredToken))
            }.verifyComplete()
    }

    object MockBearerTokenProvider : ExpirableTokenProvider {
        @Volatile
        private var isFistCall = true
        val expiredToken = JwtFixture
            .generateToken(Date(System.currentTimeMillis() - 10000)).jwtToExpirableToken()
        val notExpiredToken = JwtFixture
            .generateToken(Date(System.currentTimeMillis() + 10000)).jwtToExpirableToken()

        override fun getToken(): Mono<ExpirableToken> {
            return Mono.create {
                if (isFistCall) {
                    isFistCall = false
                    it.success(expiredToken)
                } else {
                    it.success(notExpiredToken)
                }
            }
        }
    }
}
