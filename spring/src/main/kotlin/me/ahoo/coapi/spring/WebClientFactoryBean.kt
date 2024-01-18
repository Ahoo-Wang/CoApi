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

package me.ahoo.coapi.spring

import org.springframework.beans.factory.FactoryBean
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

/**
 * @see org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction
 */
class WebClientFactoryBean(private val definition: CoApiDefinition) :
    FactoryBean<WebClient>,
    ApplicationContextAware {
    companion object {
        private val loadBalancedFilterClass = LoadBalancedExchangeFilterFunction::class.java
    }

    private lateinit var applicationContext: ApplicationContext
    override fun getObject(): WebClient {
        val clientBuilder = applicationContext.getBean(WebClient.Builder::class.java)
        clientBuilder.baseUrl(definition.baseUrl)
        val filters = buildList<ExchangeFilterFunction> {
            definition.filters.forEach { filterName ->
                val filter = applicationContext.getBean(filterName, ExchangeFilterFunction::class.java)
                add(filter)
            }
            definition.filterTypes.forEach { filterType ->
                val filter = applicationContext.getBean(filterType)
                add(filter)
            }
            if (definition.loadBalanced) {
                val hasLoadBalancedFilter = any { filter ->
                    filter is LoadBalancedExchangeFilterFunction
                }
                if (!hasLoadBalancedFilter) {
                    val loadBalancedExchangeFilterFunction = applicationContext.getBean(loadBalancedFilterClass)
                    add(loadBalancedExchangeFilterFunction)
                }
            }
        }
        filters.distinct().forEach {
            clientBuilder.filter(it)
        }
        return clientBuilder.build()
    }

    override fun getObjectType(): Class<*> {
        return WebClient::class.java
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
}
