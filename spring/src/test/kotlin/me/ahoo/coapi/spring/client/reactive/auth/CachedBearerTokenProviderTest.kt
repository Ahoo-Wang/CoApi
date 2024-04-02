package me.ahoo.coapi.spring.client.reactive.auth

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.util.*

class CachedBearerTokenProviderTest {

    @Test
    fun getBearerToken() {
        val cachedBearerTokenProvider = CachedBearerTokenProvider(MockBearerTokenProvider)
        cachedBearerTokenProvider.getBearerToken()
            .test()
            .consumeNextWith {
                // 仅当缓存当前已填充时才会评估
                assertThat(it, equalTo(MockBearerTokenProvider.expiredToken))
            }.verifyComplete()

        cachedBearerTokenProvider.getBearerToken()
            .test()
            .consumeNextWith {
                assertThat(it, equalTo(MockBearerTokenProvider.notExpiredToken))
            }.verifyComplete()
        cachedBearerTokenProvider.getBearerToken()
            .test()
            .consumeNextWith {
                assertThat(it, equalTo(MockBearerTokenProvider.notExpiredToken))
            }.verifyComplete()
        cachedBearerTokenProvider.getBearerToken()
            .test()
            .consumeNextWith {
                assertThat(it, equalTo(MockBearerTokenProvider.notExpiredToken))
            }.verifyComplete()
    }

    object MockBearerTokenProvider : BearerTokenProvider {
        @Volatile
        private var isFistCall = true
        val expiredToken = JwtFixture.generateToken(Date(System.currentTimeMillis() - 10000))
        val notExpiredToken = JwtFixture.generateToken(Date(System.currentTimeMillis() + 10000))
        override fun getBearerToken(): Mono<String> {
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
