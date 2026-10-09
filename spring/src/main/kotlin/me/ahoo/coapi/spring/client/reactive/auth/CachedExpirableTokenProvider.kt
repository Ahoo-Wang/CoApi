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

import io.github.oshai.kotlinlogging.KotlinLogging
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * Caches the token of [tokenProvider] and fetches a new one once the cached token expires within
 * [refreshBeforeExpiry]. Refreshing early keeps a token that is about to expire from being sent and
 * rejected in flight (request latency, clock skew between client and server).
 *
 * Tokens whose whole lifetime is shorter than [refreshBeforeExpiry] are fetched on every use.
 */
class CachedExpirableTokenProvider @JvmOverloads constructor(
    tokenProvider: ExpirableTokenProvider,
    private val refreshBeforeExpiry: Duration = DEFAULT_REFRESH_BEFORE_EXPIRY
) : ExpirableTokenProvider {
    companion object {
        private val log = KotlinLogging.logger {}

        /**
         * Same default as Spring Security's OAuth2 client clock skew.
         */
        val DEFAULT_REFRESH_BEFORE_EXPIRY: Duration = Duration.ofSeconds(60)
    }

    init {
        require(!refreshBeforeExpiry.isNegative) {
            "refreshBeforeExpiry must not be negative: $refreshBeforeExpiry"
        }
    }

    private val tokenCache: Mono<ExpirableToken> = tokenProvider.getToken()
        .cacheInvalidateIf {
            val refresh = it.expiresWithin(refreshBeforeExpiry)
            log.debug {
                "CacheInvalidateIf - expiresWithin($refreshBeforeExpiry):$refresh"
            }
            refresh
        }

    override fun getToken(): Mono<ExpirableToken> {
        return tokenCache
    }
}
