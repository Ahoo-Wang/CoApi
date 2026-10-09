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
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

/**
 * Builds the [WebClient] of a [CoApiDefinition], in this order:
 * base URL → configured filters ([ReactiveClientProperties]) → load balancer (if load balanced)
 * → [WebClientBuilderCustomizer] beans (ordered).
 *
 * Requires the context to resolve exactly one [WebClient.Builder] bean by type. Applications
 * registering multiple builders (e.g. a Spring Cloud `@LoadBalanced` builder alongside the default
 * one) must mark one of them as primary.
 */
class WebClientFactoryBean(definition: CoApiDefinition) : AbstractHttpClientFactoryBean<WebClient>(definition) {

    override fun getObject(): WebClient {
        val effectiveDefinition = effectiveDefinition()
        val builder = appContext.getBean(WebClient.Builder::class.java)
        builder.baseUrl(effectiveDefinition.baseUrl)
        val filters = optionalBean(ReactiveClientProperties::class.java, ReactiveClientProperties.Empty)
            .getFilter(effectiveDefinition.name)
        builder.filters { it.addAll(resolveComponents(ExchangeFilterFunction::class.java, filters)) }
        if (effectiveDefinition.loadBalanced) {
            requireLoadBalancerSupport(effectiveDefinition, LoadBalancedWebClientBuilderCustomizer.FILTER_CLASS_NAME)
            LoadBalancedWebClientBuilderCustomizer(appContext).customize(effectiveDefinition, builder)
        }
        customizers(WebClientBuilderCustomizer::class.java).forEach { it.customize(effectiveDefinition, builder) }
        return builder.build()
    }

    override fun getObjectType(): Class<*> {
        return WebClient::class.java
    }
}
