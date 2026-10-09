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

package me.ahoo.coapi.spring.client.sync

import me.ahoo.coapi.spring.CoApiDefinition
import me.ahoo.coapi.spring.client.AbstractHttpClientFactoryBean
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient

/**
 * Builds the [RestClient] of a [CoApiDefinition], in this order:
 * base URL → configured interceptors ([SyncClientProperties]) → load balancer (if load balanced)
 * → [RestClientBuilderCustomizer] beans (ordered).
 *
 * Requires the context to resolve exactly one [RestClient.Builder] bean by type. Applications
 * registering multiple builders (e.g. a Spring Cloud `@LoadBalanced` builder alongside the default
 * one) must mark one of them as primary.
 */
class RestClientFactoryBean(definition: CoApiDefinition) : AbstractHttpClientFactoryBean<RestClient>(definition) {

    override fun getObject(): RestClient {
        val effectiveDefinition = effectiveDefinition()
        val builder = appContext.getBean(RestClient.Builder::class.java)
        builder.baseUrl(effectiveDefinition.baseUrl)
        val interceptors = optionalBean(SyncClientProperties::class.java, SyncClientProperties.Empty)
            .getInterceptor(effectiveDefinition.name)
        builder.requestInterceptors {
            it.addAll(resolveComponents(ClientHttpRequestInterceptor::class.java, interceptors))
        }
        if (effectiveDefinition.loadBalanced) {
            requireLoadBalancerSupport(
                effectiveDefinition,
                LoadBalancedRestClientBuilderCustomizer.INTERCEPTOR_CLASS_NAME
            )
            LoadBalancedRestClientBuilderCustomizer(appContext).customize(effectiveDefinition, builder)
        }
        customizers(RestClientBuilderCustomizer::class.java).forEach { it.customize(effectiveDefinition, builder) }
        return builder.build()
    }

    override fun getObjectType(): Class<*> {
        return RestClient::class.java
    }
}
