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

import me.ahoo.coapi.api.CoApi
import me.ahoo.coapi.spring.AbstractCoApiRegistrar
import me.ahoo.coapi.spring.CoApiDefinition
import me.ahoo.coapi.spring.CoApiDefinition.Companion.toCoApiDefinition
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBeanProvider
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.util.ClassUtils

class AutoCoApiRegistrar : AbstractCoApiRegistrar(), BeanClassLoaderAware, ResourceLoaderAware {

    /**
     * Scanned interfaces are loaded with the application's bean class loader, like Spring's own component
     * scanning. CoApi's own class loader may differ, e.g. under Spring Boot DevTools' RestartClassLoader,
     * which would produce proxy types that do not match the application's injection points.
     */
    private var beanClassLoader: ClassLoader? = null
    private var resourceLoader: ResourceLoader? = null

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.beanClassLoader = classLoader
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
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
     * being registered and before any `BeanPostProcessor` exists. Hence: lookup never triggers eager
     * initialization for type matching, instance `@Bean` methods are rejected (see [requireStaticFactoryMethod]),
     * and placeholders in [CoApiDefinition.baseUrl] are resolved here, like on the `@CoApi` annotation.
     */
    private fun getCoApiDefinitionBeans(): List<CoApiDefinition> {
        val beanFactory = appContext as? ConfigurableListableBeanFactory
            ?: return appContext.getBeanProvider<CoApiDefinition>().map { it.resolvePlaceholders(env) }
        return beanFactory.getBeanNamesForType(CoApiDefinition::class.java, true, false).map { beanName ->
            beanFactory.requireStaticFactoryMethod(beanName)
            beanFactory.getBean(beanName, CoApiDefinition::class.java).resolvePlaceholders(env)
        }
    }

    /**
     * A non-static `@Bean` method would force its configuration class to be created here, too early: its
     * `@Autowired`/`@Value` fields would not be injected and its `@Bean` methods would not be proxied.
     * Same pitfall as Spring's own `BeanFactoryPostProcessor` beans, but failing fast instead of degrading silently.
     */
    private fun ConfigurableListableBeanFactory.requireStaticFactoryMethod(beanName: String) {
        val factoryBeanName = getBeanDefinition(beanName).factoryBeanName ?: return
        throw IllegalStateException(
            "CoApiDefinition bean [$beanName] is declared by a non-static @Bean method on [$factoryBeanName]. " +
                "CoApiDefinition beans are read before bean post-processing, which would create [$factoryBeanName] " +
                "too early (no @Autowired/@Value injection, no @Bean proxying). Declare the method static " +
                "(Kotlin: @JvmStatic in a companion object); placeholders in baseUrl are resolved by CoApi."
        )
    }

    private fun Set<String>.toApiClientDefinitions(): Set<CoApiDefinition> {
        val scanner = ApiClientScanner(false, env)
        resourceLoader?.let { scanner.resourceLoader = it }
        return flatMap { basePackage ->
            scanner.findCandidateComponents(basePackage)
        }.map { beanDefinition ->
            ClassUtils.forName(requireNotNull(beanDefinition.beanClassName), beanClassLoader)
                .toCoApiDefinition(env)
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
