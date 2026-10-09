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

import io.mockk.mockk
import io.mockk.verify
import me.ahoo.coapi.spring.client.reactive.auth.BearerHeaderValueMapper.withBearerPrefix
import me.ahoo.coapi.spring.client.reactive.auth.ExpirableToken.Companion.jwtToExpirableToken
import me.ahoo.test.asserts.assert
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.net.URI
import java.util.*

class BearerTokenFilterTest {

    @Test
    fun filter() {
        val clientRequest = ClientRequest
            .create(HttpMethod.GET, URI.create("http://localhost"))
            .build()
        val jwtToken = JwtFixture.generateToken(Date())
        val nextException = ExchangeFunction { request ->
            request.headers().getFirst(HttpHeaders.AUTHORIZATION).assert().isEqualTo(jwtToken.withBearerPrefix())
            Mono.empty()
        }
        val tokenProvider = object : ExpirableTokenProvider {
            override fun getToken(): Mono<ExpirableToken> {
                return Mono.just(jwtToken.jwtToExpirableToken())
            }
        }
        val bearerTokenFilter = BearerTokenFilter(tokenProvider)
        bearerTokenFilter.filter(clientRequest, nextException)
            .test()
            .verifyComplete()
    }

    @Test
    fun filter_ContainsKey() {
        val jwtToken = JwtFixture.generateToken(Date())
        val clientRequest = ClientRequest
            .create(HttpMethod.GET, URI.create("http://localhost"))
            .headers {
                it.setBearerAuth(jwtToken)
            }
            .build()

        val nextException = ExchangeFunction { request ->
            Mono.empty()
        }
        val tokenProvider = mockk<ExpirableTokenProvider>()
        val bearerTokenFilter = BearerTokenFilter(tokenProvider)
        bearerTokenFilter.filter(clientRequest, nextException)
            .test()
            .verifyComplete()
        verify(exactly = 0) { tokenProvider.getHeaderValue() }
    }
}
