package me.ahoo.coapi.spring.client.reactive.auth

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.net.URI
import java.util.Date

class BearerTokenFilterTest {

    @Test
    fun filter() {
        val clientRequest = ClientRequest
            .create(HttpMethod.GET, URI.create("http://localhost"))
            .build()
        val jwtToken = JwtFixture.generateToken(Date())
        val nextException = ExchangeFunction { request ->
            assertThat(request.headers().getFirst("Authorization"), equalTo("Bearer $jwtToken"))
            Mono.empty()
        }
        val tokenProvider = BearerTokenProvider { Mono.just(jwtToken) }
        val bearerTokenFilter = BearerTokenFilter(tokenProvider)
        bearerTokenFilter.filter(clientRequest, nextException)
            .test()
            .verifyComplete()
    }
}
