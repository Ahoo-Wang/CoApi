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
import reactor.core.publisher.Mono

data class ExpirableToken(val token: String, val expiresAt: Long) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAt

    companion object {
        private val jwtParser = JWT()
        fun String.jwtToExpirableToken(): ExpirableToken {
            val decodedJWT = jwtParser.decodeJwt(this)
            val expiresAt = checkNotNull(decodedJWT.expiresAt)
            return ExpirableToken(this, expiresAt.time)
        }
    }
}

interface ExpirableTokenProvider : HeaderValueProvider {
    fun getToken(): Mono<ExpirableToken>

    override fun getHeaderValue(): Mono<String> {
        return getToken().map { it.token }
    }
}
