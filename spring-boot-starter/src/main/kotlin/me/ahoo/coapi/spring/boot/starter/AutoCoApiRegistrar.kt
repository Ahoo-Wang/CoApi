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

package me.ahoo.coapi.spring.boot.starter

import io.github.oshai.kotlinlogging.KotlinLogging
import me.ahoo.coapi.api.CoApi
import me.ahoo.coapi.spring.AbstractCoApiRegistrar
import me.ahoo.coapi.spring.CoApiDefinition
import me.ahoo.coapi.spring.CoApiDefinition.Companion.toCoApiDefinition
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBeanProvider
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter

class AutoCoApiRegistrar : AbstractCoApiRegistrar() {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    /**
     * Binds `coapi.base-packages` in either form (comma-separated or indexed list) with Boot's
     * relaxed binding. The registrar runs before [CoApiProperties] is bound, hence the direct binding.
     */
    private fun getCoApiBasePackages(): Set<String> {
        return Binder.get(env)
            .bind(CoApiProperties.COAPI_BASE_PACKAGES, Bindable.listOf(String::class.java))
            .orElse(null)
            .orEmpty()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    private fun getScanBasePackages(): Set<String> {
        val coApiBasePackages = getCoApiBasePackages()
        if (AutoConfigurationPackages.has(appContext).not()) {
            return coApiBasePackages
        }
        return AutoConfigurationPackages.get(appContext).toSet() + coApiBasePackages
    }

    override fun getCoApiDefinitions(importingClassMetadata: AnnotationMetadata): Set<CoApiDefinition> {
        val scanBasePackages = getScanBasePackages()
        return scanBasePackages.toApiClientDefinitions() + getCoApiDefinitionBeans()
    }

    /**
     * Collects [CoApiDefinition] beans. They have to be instantiated here, while bean definitions are still
     * being registered and before any `BeanPostProcessor` exists, so lookup never triggers eager
     * initialization for type matching, and instance `@Bean` methods are flagged (see [warnIfInstanceFactoryMethod]).
     */
    private fun getCoApiDefinitionBeans(): List<CoApiDefinition> {
        val beanFactory = appContext as? ConfigurableListableBeanFactory
            ?: return appContext.getBeanProvider<CoApiDefinition>().toList()
        return beanFactory.getBeanNamesForType(CoApiDefinition::class.java, true, false).map { beanName ->
            beanFactory.warnIfInstanceFactoryMethod(beanName)
            beanFactory.getBean(beanName, CoApiDefinition::class.java)
        }
    }

    /**
     * A non-static `@Bean` method forces its configuration class to be created here, too early: its
     * `@Autowired`/`@Value` fields are not injected and its `@Bean` methods are not proxied.
     * Same pitfall and remedy as Spring's own `BeanFactoryPostProcessor` beans: declare the method static.
     */
    private fun ConfigurableListableBeanFactory.warnIfInstanceFactoryMethod(beanName: String) {
        val factoryBeanName = getBeanDefinition(beanName).factoryBeanName ?: return
        log.warn {
            "CoApiDefinition bean [$beanName] is declared by a non-static @Bean method on [$factoryBeanName], " +
                "which is instantiated before bean post-processing: its @Autowired/@Value fields are not injected " +
                "and its @Bean methods are not proxied. Declare the method static " +
                "(Kotlin: @JvmStatic in a companion object) and resolve placeholders via an Environment parameter."
        }
    }

    private fun Set<String>.toApiClientDefinitions(): Set<CoApiDefinition> {
        val scanner = ApiClientScanner(false, env)
        return flatMap { basePackage ->
            scanner.findCandidateComponents(basePackage)
        }.map { beanDefinition ->
            Class.forName(beanDefinition.beanClassName).toCoApiDefinition(env)
        }.toSet()
    }
}

class ApiClientScanner(useDefaultFilters: Boolean, environment: Environment) :
    ClassPathScanningCandidateComponentProvider(useDefaultFilters, environment) {
    init {
        addIncludeFilter(AnnotationTypeFilter(CoApi::class.java))
    }

    override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {
        return beanDefinition.metadata.isInterface
    }
}
