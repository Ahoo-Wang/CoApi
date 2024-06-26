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
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

class WebClientFactoryBean(definition: CoApiDefinition) :
    AbstractWebClientFactoryBean(definition) {

    override val builderCustomizer: WebClientBuilderCustomizer by lazy {
        if (!loadBalanced()) {
            return@lazy WebClientBuilderCustomizer.NoOp
        }
        return@lazy LoadBalancedWebClientBuilderCustomizer()
    }

    inner class LoadBalancedWebClientBuilderCustomizer : WebClientBuilderCustomizer {
        override fun customize(coApiDefinition: CoApiDefinition, builder: WebClient.Builder) {
            builder.filters {
                val hasLoadBalancedFilter = it.any { filter ->
                    filter is LoadBalancedExchangeFilterFunction
                }
                if (!hasLoadBalancedFilter) {
                    val loadBalancedExchangeFilterFunction =
                        appContext.getBean(LoadBalancedExchangeFilterFunction::class.java)
                    it.add(loadBalancedExchangeFilterFunction)
                }
            }
        }
    }
}
