/*
 * Copyright [2022-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.coapi.spring.client.reactive.auth

import me.ahoo.coapi.spring.client.reactive.auth.ExpirableToken.Companion.jwtToExpirableToken
import me.ahoo.test.asserts.assert
import me.ahoo.test.asserts.assertThrownBy
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class CachedExpirableTokenProviderTest {

    @Test
    fun getBearerToken() {
        val cachedExpirableTokenProvider = CachedExpirableTokenProvider(MockBearerTokenProvider)
        cachedExpirableTokenProvider.getToken()
            .test()
            .consumeNextWith {
                // 仅当缓存当前已填充时才会评估
                it.assert().isEqualTo(MockBearerTokenProvider.expiredToken)
            }.verifyComplete()

        cachedExpirableTokenProvider.getToken()
            .test()
            .consumeNextWith {
                it.assert().isEqualTo(MockBearerTokenProvider.notExpiredToken)
            }.verifyComplete()
        cachedExpirableTokenProvider.getToken()
            .test()
            .consumeNextWith {
                it.assert().isEqualTo(MockBearerTokenProvider.notExpiredToken)
            }.verifyComplete()
        cachedExpirableTokenProvider.getToken()
            .test()
            .consumeNextWith {
                it.assert().isEqualTo(MockBearerTokenProvider.notExpiredToken)
            }.verifyComplete()
    }

    private class CountingTokenProvider(private val expireIn: Duration) : ExpirableTokenProvider {
        val calls = AtomicInteger()

        override fun getToken(): Mono<ExpirableToken> {
            return Mono.fromCallable {
                calls.incrementAndGet()
                ExpirableToken("token", System.currentTimeMillis() + expireIn.toMillis())
            }
        }
    }

    @Test
    fun `token expiring within the refresh margin should be refreshed`() {
        val tokenProvider = CountingTokenProvider(expireIn = Duration.ofSeconds(30))
        val cached = CachedExpirableTokenProvider(tokenProvider, refreshBeforeExpiry = Duration.ofSeconds(60))

        repeat(3) { cached.getToken().block() }

        tokenProvider.calls.get().assert().isEqualTo(3)
    }

    @Test
    fun `token valid beyond the refresh margin should be cached`() {
        val tokenProvider = CountingTokenProvider(expireIn = Duration.ofMinutes(10))
        val cached = CachedExpirableTokenProvider(tokenProvider)

        repeat(3) { cached.getToken().block() }

        tokenProvider.calls.get().assert().isEqualTo(1)
    }

    @Test
    fun `negative refresh margin should be rejected`() {
        assertThrownBy<IllegalArgumentException> {
            CachedExpirableTokenProvider(CountingTokenProvider(Duration.ofMinutes(10)), Duration.ofSeconds(-1))
        }
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
