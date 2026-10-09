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

import io.github.oshai.kotlinlogging.KotlinLogging
import me.ahoo.coapi.spring.client.reactive.ReactiveHttpExchangeAdapterFactory
import me.ahoo.coapi.spring.client.reactive.WebClientFactoryBean
import me.ahoo.coapi.spring.client.sync.RestClientFactoryBean
import me.ahoo.coapi.spring.client.sync.SyncHttpExchangeAdapterFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry

/**
 * Registers the bean definitions that back a set of [CoApiDefinition]s for a [ClientMode]:
 * the shared [HttpExchangeAdapterFactory], and per definition an HTTP client bean
 * (`WebClient` or `RestClient`) plus the CoApi proxy bean.
 *
 * Bean definitions that already exist are kept, so applications can override any of them.
 */
class CoApiRegistrar(private val registry: BeanDefinitionRegistry, private val clientMode: ClientMode) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    private val isSync: Boolean = clientMode == ClientMode.SYNC

    fun register(coApiDefinitions: Set<CoApiDefinition>) {
        coApiDefinitions
            .groupBy { it.name }
            .values
            .firstOrNull { it.size > 1 }
            ?.let { conflicting ->
                throw IllegalStateException(
                    "Duplicate CoApi name [${conflicting.first().name}]: " +
                        "${conflicting.map { it.apiType.name }}. " +
                        "The client name is derived from @CoApi.name (or the interface simple name) " +
                        "- make the names unique."
                )
            }
        registerHttpExchangeAdapterFactory()
        coApiDefinitions.forEach {
            register(it)
        }
    }

    fun register(coApiDefinition: CoApiDefinition) {
        val (clientKind, clientFactoryBeanClass) = if (isSync) {
            "RestClient" to RestClientFactoryBean::class.java
        } else {
            "WebClient" to WebClientFactoryBean::class.java
        }
        registerIfAbsent(clientKind, coApiDefinition.httpClientBeanName, clientFactoryBeanClass, coApiDefinition)
        registerIfAbsent("CoApi", coApiDefinition.coApiBeanName, CoApiFactoryBean::class.java, coApiDefinition)
    }

    private fun registerHttpExchangeAdapterFactory() {
        if (registry.containsBeanDefinition(HttpExchangeAdapterFactory.BEAN_NAME)) {
            return
        }
        val httpExchangeAdapterFactoryClass = if (isSync) {
            SyncHttpExchangeAdapterFactory::class.java
        } else {
            ReactiveHttpExchangeAdapterFactory::class.java
        }
        val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(httpExchangeAdapterFactoryClass).beanDefinition
        registry.registerBeanDefinition(HttpExchangeAdapterFactory.BEAN_NAME, beanDefinition)
    }

    private fun registerIfAbsent(
        kind: String,
        beanName: String,
        factoryBeanClass: Class<*>,
        coApiDefinition: CoApiDefinition
    ) {
        log.info {
            "Register $kind [$beanName]."
        }
        if (registry.containsBeanDefinition(beanName)) {
            log.warn {
                "$kind [$beanName] already exists - Ignore."
            }
            return
        }
        val beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(factoryBeanClass)
            .addConstructorArgValue(coApiDefinition)
            .beanDefinition
        registry.registerBeanDefinition(beanName, beanDefinition)
    }
}
