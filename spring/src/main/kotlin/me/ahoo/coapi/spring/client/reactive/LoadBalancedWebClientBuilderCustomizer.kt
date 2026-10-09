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
import org.springframework.beans.factory.BeanFactory
import org.springframework.cloud.client.loadbalancer.reactive.DeferringLoadBalancerExchangeFilterFunction
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

/**
 * Adds Spring Cloud's [LoadBalancedExchangeFilterFunction], unless the builder already carries a
 * load-balancing filter (e.g. Spring Cloud's own deferring one).
 *
 * Only loaded when a client is load balanced, so Spring Cloud stays an optional dependency.
 */
internal class LoadBalancedWebClientBuilderCustomizer(
    private val beanFactory: BeanFactory
) : WebClientBuilderCustomizer {
    companion object {
        const val FILTER_CLASS_NAME =
            "org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction"
    }

    override fun customize(coApiDefinition: CoApiDefinition, builder: WebClient.Builder) {
        builder.filters { filters ->
            val hasLoadBalancedFilter = filters.any { filter ->
                filter is LoadBalancedExchangeFilterFunction || filter is DeferringLoadBalancerExchangeFilterFunction<*>
            }
            if (!hasLoadBalancedFilter) {
                filters.add(beanFactory.getBean(LoadBalancedExchangeFilterFunction::class.java))
            }
        }
    }
}
