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

import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

open class HeaderSetFilter(
    private val headerName: String,
    private val headerValueProvider: HeaderValueProvider,
    private val headerValueMapper: HeaderValueMapper = HeaderValueMapper.IDENTITY
) : ExchangeFilterFunction {
    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        if (request.headers().containsKey(headerName)) {
            return next.exchange(request)
        }
        return headerValueProvider.getHeaderValue()
            .map { headerValue ->
                ClientRequest.from(request)
                    .headers { headers ->
                        headers[headerName] = headerValueMapper.map(headerValue)
                    }
                    .build()
            }
            .flatMap { next.exchange(it) }
    }
}

fun interface HeaderValueProvider {
    fun getHeaderValue(): Mono<String>
}

fun interface HeaderValueMapper {
    companion object {
        val IDENTITY = HeaderValueMapper { it }
    }

    fun map(headerValue: String): String
}
