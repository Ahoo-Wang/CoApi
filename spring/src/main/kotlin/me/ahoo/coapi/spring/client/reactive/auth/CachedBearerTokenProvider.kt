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

import com.auth0.jwt.JWT
import me.ahoo.coapi.spring.client.reactive.auth.CachedBearerTokenProvider.JwtToken.Companion.toJwtToken
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.util.*

class CachedBearerTokenProvider(tokenProvider: BearerTokenProvider) : BearerTokenProvider {
    companion object {
        private val log = LoggerFactory.getLogger(CachedBearerTokenProvider::class.java)
    }

    private val tokenCache: Mono<String> = tokenProvider.getBearerToken()
        .map {
            it.toJwtToken()
        }
        .cacheInvalidateIf {
            if (log.isDebugEnabled) {
                log.debug("CacheInvalidateIf - isExpired:${it.isExpired}")
            }
            it.isExpired
        }.map {
            it.accessToken
        }

    override fun getBearerToken(): Mono<String> {
        return tokenCache
    }

    data class JwtToken(val accessToken: String, val expiresAt: Date) {
        val isExpired: Boolean
            get() = Date().after(expiresAt)

        companion object {
            private val jwtParser = JWT()
            fun String.toJwtToken(): JwtToken {
                val decodedJWT = jwtParser.decodeJwt(this)
                val expiresAt = checkNotNull(decodedJWT.expiresAt)
                return JwtToken(this, expiresAt)
            }
        }
    }
}
