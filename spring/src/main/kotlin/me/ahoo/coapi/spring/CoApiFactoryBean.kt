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
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.web.service.invoker.HttpServiceProxyFactory

class CoApiFactoryBean(
    private val coApiDefinition: CoApiDefinition
) : FactoryBean<Any>, ApplicationContextAware {

    private lateinit var applicationContext: ApplicationContext
    override fun getObject(): Any {
        val httpExchangeAdapterFactory = applicationContext.getBean(HttpExchangeAdapterFactory::class.java)
        val httpExchangeAdapter = httpExchangeAdapterFactory.create(
            beanFactory = applicationContext,
            httpClientName = coApiDefinition.httpClientBeanName
        )
        val httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(httpExchangeAdapter).build()
        return httpServiceProxyFactory.createClient(coApiDefinition.apiType)
    }

    override fun getObjectType(): Class<*> {
        return coApiDefinition.apiType
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
}
