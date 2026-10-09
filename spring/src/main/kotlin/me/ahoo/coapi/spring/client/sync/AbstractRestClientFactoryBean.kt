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
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.getBean
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient

abstract class AbstractRestClientFactoryBean(override val definition: CoApiDefinition) :
    AbstractHttpClientFactoryBean(),
    FactoryBean<RestClient> {

    protected open val builderCustomizer: RestClientBuilderCustomizer = RestClientBuilderCustomizer.NoOp

    /**
     * Build the RestClient for [definition].
     *
     * Requires the context to resolve exactly one [RestClient.Builder] bean by type.
     * Applications registering multiple builders (e.g. a Spring Cloud `@LoadBalanced`
     * builder alongside the default one) must mark one of them as primary.
     */
    override fun getObject(): RestClient {
        val clientBuilder = appContext
            .getBean<RestClient.Builder>()
        clientBuilder.baseUrl(getBaseUrl())
        val interceptorDefinition = clientProperties.getInterceptor(definition.name)
        clientBuilder.requestInterceptors {
            it.addAll(
                resolveBeans(
                    ClientHttpRequestInterceptor::class.java,
                    interceptorDefinition.names,
                    interceptorDefinition.types
                )
            )
        }
        builderCustomizer.customize(definition, clientBuilder)
        appContext.getBeanProvider(RestClientBuilderCustomizer::class.java)
            .orderedStream()
            .forEach { customizer ->
                customizer.customize(definition, clientBuilder)
            }
        return clientBuilder.build()
    }

    override fun getObjectType(): Class<*> {
        return RestClient::class.java
    }
}
