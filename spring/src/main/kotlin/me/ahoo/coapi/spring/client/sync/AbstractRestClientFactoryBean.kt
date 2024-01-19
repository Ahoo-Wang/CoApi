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
import me.ahoo.coapi.spring.client.ClientProperties
import org.springframework.beans.factory.FactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient

abstract class AbstractRestClientFactoryBean(private val definition: CoApiDefinition) :
    FactoryBean<RestClient>,
    ApplicationContextAware {

    protected lateinit var appContext: ApplicationContext

    protected open val interceptorCustomizer: (MutableList<ClientHttpRequestInterceptor>) -> Unit = {}

    override fun getObject(): RestClient {
        val clientBuilder = appContext.getBean(RestClient.Builder::class.java)
        clientBuilder.baseUrl(definition.baseUrl)
        val clientProperties = appContext.getBean(ClientProperties::class.java)
        val interceptorDefinition = clientProperties.getInterceptor(definition.name)
        val requestInterceptors = getRequestInterceptors(interceptorDefinition)
        requestInterceptors.distinct().forEach {
            clientBuilder.requestInterceptor(it)
        }
        return clientBuilder.build()
    }

    private fun getRequestInterceptors(
        interceptorDefinition: ClientProperties.InterceptorDefinition
    ): List<ClientHttpRequestInterceptor> {
        return buildList {
            interceptorDefinition.names.forEach { interceptorName ->
                val interceptor = appContext.getBean(interceptorName, ClientHttpRequestInterceptor::class.java)
                add(interceptor)
            }
            interceptorDefinition.types.forEach { interceptorType ->
                val interceptor = appContext.getBean(interceptorType)
                add(interceptor)
            }
            interceptorCustomizer(this)
        }
    }

    override fun getObjectType(): Class<*> {
        return RestClient::class.java
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.appContext = applicationContext
    }
}