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

import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

class CachedExpirableTokenProvider(tokenProvider: ExpirableTokenProvider) : ExpirableTokenProvider {
    companion object {
        private val log = LoggerFactory.getLogger(CachedExpirableTokenProvider::class.java)
    }

    private val tokenCache: Mono<ExpirableToken> = tokenProvider.getToken()
        .cacheInvalidateIf {
            if (log.isDebugEnabled) {
                log.debug("CacheInvalidateIf - isExpired:${it.isExpired}")
            }
            it.isExpired
        }

    override fun getToken(): Mono<ExpirableToken> {
        return tokenCache
    }
}
