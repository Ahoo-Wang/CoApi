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
import me.ahoo.coapi.spring.client.ClientProperties
import org.springframework.beans.factory.FactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

abstract class AbstractWebClientFactoryBean(private val definition: CoApiDefinition) :
    FactoryBean<WebClient>,
    ApplicationContextAware {
    protected lateinit var appContext: ApplicationContext

    protected open val filterCustomizer: (MutableList<ExchangeFilterFunction>) -> Unit = {}

    override fun getObjectType(): Class<*> {
        return WebClient::class.java
    }

    override fun getObject(): WebClient {
        val clientBuilder = appContext.getBean(WebClient.Builder::class.java)
        clientBuilder.baseUrl(definition.baseUrl)
        val clientProperties = appContext.getBean(ClientProperties::class.java)
        val filterDefinition = clientProperties.getFilter(definition.name)
        val filters = getFilters(filterDefinition)
        filters.distinct().forEach {
            clientBuilder.filter(it)
        }
        return clientBuilder.build()
    }

    private fun getFilters(filterDefinition: ClientProperties.FilterDefinition): List<ExchangeFilterFunction> {
        return buildList {
            filterDefinition.names.forEach { filterName ->
                val filter = appContext.getBean(filterName, ExchangeFilterFunction::class.java)
                add(filter)
            }
            filterDefinition.types.forEach { filterType ->
                val filter = appContext.getBean(filterType)
                add(filter)
            }
            filterCustomizer(this)
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.appContext = applicationContext
    }
}
