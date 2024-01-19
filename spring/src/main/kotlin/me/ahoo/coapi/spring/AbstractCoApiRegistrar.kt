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

import me.ahoo.coapi.spring.ClientMode.Companion.inferClientMode
import me.ahoo.coapi.spring.client.reactive.ReactiveHttpExchangeAdapterFactory
import me.ahoo.coapi.spring.client.sync.SyncHttpExchangeAdapterFactory
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotationMetadata

abstract class AbstractCoApiRegistrar : ImportBeanDefinitionRegistrar, EnvironmentAware, BeanFactoryAware {

    protected lateinit var env: Environment
    protected lateinit var appContext: BeanFactory
    override fun setEnvironment(environment: Environment) {
        this.env = environment
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.appContext = beanFactory
    }

    abstract fun getCoApiDefinitions(importingClassMetadata: AnnotationMetadata): Set<CoApiDefinition>

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val clientMode = inferClientMode {
            env.getProperty(it)
        }
        registerHttpExchangeAdapterFactory(clientMode, registry)
        val coApiRegistrar = CoApiRegistrar(registry, clientMode)
        val apiDefinitions = getCoApiDefinitions(importingClassMetadata)
        coApiRegistrar.register(apiDefinitions)
    }

    private fun registerHttpExchangeAdapterFactory(clientMode: ClientMode, registry: BeanDefinitionRegistry) {
        if (registry.containsBeanDefinition(HttpExchangeAdapterFactory.BEAN_NAME)) {
            return
        }
        val httpExchangeAdapterFactoryClass = if (clientMode == ClientMode.SYNC) {
            SyncHttpExchangeAdapterFactory::class.java
        } else {
            ReactiveHttpExchangeAdapterFactory::class.java
        }
        val beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(httpExchangeAdapterFactoryClass)
        registry.registerBeanDefinition(HttpExchangeAdapterFactory.BEAN_NAME, beanDefinitionBuilder.beanDefinition)
    }
}
