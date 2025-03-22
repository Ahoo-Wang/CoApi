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
import me.ahoo.coapi.spring.client.reactive.WebClientFactoryBean
import me.ahoo.coapi.spring.client.sync.RestClientFactoryBean
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry

class CoApiRegistrar(private val registry: BeanDefinitionRegistry, private val clientMode: ClientMode) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun register(coApiDefinitions: Set<CoApiDefinition>) {
        coApiDefinitions.forEach {
            register(it)
        }
    }

    fun register(coApiDefinition: CoApiDefinition) {
        if (clientMode == ClientMode.SYNC) {
            registerRestClient(registry, coApiDefinition)
        } else {
            registerWebClient(registry, coApiDefinition)
        }
        registerApiClient(registry, coApiDefinition)
    }

    private fun registerRestClient(registry: BeanDefinitionRegistry, coApiDefinition: CoApiDefinition) {
        log.info {
            "Register RestClient [${coApiDefinition.httpClientBeanName}]."
        }
        if (registry.containsBeanDefinition(coApiDefinition.httpClientBeanName)) {
            log.warn {
                "RestClient [${coApiDefinition.httpClientBeanName}] already exists - Ignore."
            }
            return
        }
        val clientFactoryBeanClass = RestClientFactoryBean::class.java
        val beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clientFactoryBeanClass)
        beanDefinitionBuilder.addConstructorArgValue(coApiDefinition)
        registry.registerBeanDefinition(coApiDefinition.httpClientBeanName, beanDefinitionBuilder.beanDefinition)
    }

    private fun registerWebClient(registry: BeanDefinitionRegistry, coApiDefinition: CoApiDefinition) {
        log.info {
            "Register WebClient [${coApiDefinition.httpClientBeanName}]."
        }
        if (registry.containsBeanDefinition(coApiDefinition.httpClientBeanName)) {
            log.warn {
                "WebClient [${coApiDefinition.httpClientBeanName}] already exists - Ignore."
            }
            return
        }
        val clientFactoryBeanClass = WebClientFactoryBean::class.java
        val beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clientFactoryBeanClass)
        beanDefinitionBuilder.addConstructorArgValue(coApiDefinition)
        registry.registerBeanDefinition(coApiDefinition.httpClientBeanName, beanDefinitionBuilder.beanDefinition)
    }

    private fun registerApiClient(registry: BeanDefinitionRegistry, coApiDefinition: CoApiDefinition) {
        log.info {
            "Register CoApi [${coApiDefinition.coApiBeanName}]."
        }
        if (registry.containsBeanDefinition(coApiDefinition.coApiBeanName)) {
            log.warn {
                "CoApi [${coApiDefinition.coApiBeanName}] already exists - Ignore."
            }
            return
        }
        val beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(CoApiFactoryBean::class.java)
        beanDefinitionBuilder.addConstructorArgValue(coApiDefinition)
        registry.registerBeanDefinition(coApiDefinition.coApiBeanName, beanDefinitionBuilder.beanDefinition)
    }
}
