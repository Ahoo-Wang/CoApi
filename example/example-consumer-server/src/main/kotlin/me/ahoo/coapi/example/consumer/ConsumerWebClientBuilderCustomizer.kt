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

package me.ahoo.coapi.example.consumer

import me.ahoo.coapi.spring.CoApiDefinition
import me.ahoo.coapi.spring.client.reactive.WebClientBuilderCustomizer
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Service
class ConsumerWebClientBuilderCustomizer : WebClientBuilderCustomizer {
    override fun customize(
        coApiDefinition: CoApiDefinition,
        builder: WebClient.Builder
    ) {
        /**
         * https://github.com/reactor/reactor-netty/issues/388#issuecomment-704069492
         *
         * https://github.com/reactor/reactor-netty/issues/1774#issuecomment-908066283
         */
        val connectionProvider = ConnectionProvider.builder(coApiDefinition.name)
            .maxConnections(500)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofSeconds(120)).build()
        val httpClient = HttpClient.create(connectionProvider)
        val clientHttpConnector = ReactorClientHttpConnector(httpClient)
        builder.clientConnector(clientHttpConnector)
    }
}