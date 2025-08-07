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

package me.ahoo.coapi.spring.client.reactive

import me.ahoo.coapi.spring.CoApiDefinition
import me.ahoo.coapi.spring.client.AbstractHttpClientFactoryBean
import me.ahoo.coapi.spring.client.ClientProperties
import org.springframework.beans.factory.FactoryBean
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

/**
 * @see reactor.netty.resources.ConnectionProvider
 * @see org.springframework.boot.autoconfigure.web.reactive.function.client.ReactorClientHttpConnectorFactory
 * @see org.springframework.http.client.ReactorResourceFactory
 */
abstract class AbstractWebClientFactoryBean(override val definition: CoApiDefinition) :
    AbstractHttpClientFactoryBean(),
    FactoryBean<WebClient> {

    protected open val builderCustomizer: WebClientBuilderCustomizer = WebClientBuilderCustomizer.NoOp

    override fun getObjectType(): Class<*> {
        return WebClient::class.java
    }

    override fun getObject(): WebClient {
        val clientBuilder = appContext
            .getBean(WebClient.Builder::class.java)
        val clientProperties = appContext.getBean(ClientProperties::class.java)
        val baseUrl = getBaseUrl()
        clientBuilder.baseUrl(baseUrl)
        val filterDefinition = clientProperties.getFilter(definition.name)
        clientBuilder.filters {
            filterDefinition.initFilters(it)
        }
        builderCustomizer.customize(definition, clientBuilder)
        appContext.getBeanProvider(WebClientBuilderCustomizer::class.java)
            .orderedStream()
            .forEach { customizer ->
                customizer.customize(definition, clientBuilder)
            }
        return clientBuilder.build()
    }

    private fun ClientProperties.FilterDefinition.initFilters(filters: MutableList<ExchangeFilterFunction>) {
        names.forEach { filterName ->
            val filter = appContext.getBean(filterName, ExchangeFilterFunction::class.java)
            filters.add(filter)
        }
        types.forEach { filterType ->
            val filter = appContext.getBean(filterType)
            filters.add(filter)
        }
    }
}
