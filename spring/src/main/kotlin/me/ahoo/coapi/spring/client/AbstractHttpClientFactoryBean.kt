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

package me.ahoo.coapi.spring.client

import me.ahoo.coapi.spring.CoApiDefinition
import org.springframework.beans.factory.FactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.util.ClassUtils

/**
 * Shared plumbing of the per-[CoApiDefinition] HTTP client factory beans.
 *
 * The clients are built from the *effective* definition ([ClientProperties] overrides applied), which is
 * also what every [HttpClientBuilderCustomizer] receives. Extend clients through customizer beans,
 * not by subclassing the factory beans.
 */
abstract class AbstractHttpClientFactoryBean<Client : Any>(
    val definition: CoApiDefinition
) : FactoryBean<Client>, ApplicationContextAware {

    protected lateinit var appContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.appContext = applicationContext
    }

    /**
     * [definition] with the optional [ClientProperties] overrides applied.
     */
    fun effectiveDefinition(): CoApiDefinition {
        return optionalBean(ClientProperties::class.java, ClientProperties.Empty).resolve(definition)
    }

    protected fun <T : Any> optionalBean(type: Class<T>, fallback: T): T {
        return appContext.getBeanProvider(type).getIfAvailable { fallback }
    }

    /**
     * Resolves the referenced components by bean name first, then by bean type, preserving the configured order.
     */
    protected fun <T : Any> resolveComponents(type: Class<T>, components: ComponentDefinition<T>): List<T> {
        return components.names.map { appContext.getBean(it, type) } + components.types.map { appContext.getBean(it) }
    }

    protected fun <C : HttpClientBuilderCustomizer<*>> customizers(type: Class<C>): List<C> {
        return appContext.getBeanProvider(type).orderedStream().toList()
    }

    /**
     * Fails fast with an actionable message instead of a `NoClassDefFoundError` deep inside client creation.
     */
    protected fun requireLoadBalancerSupport(effectiveDefinition: CoApiDefinition, loadBalancerClassName: String) {
        check(ClassUtils.isPresent(loadBalancerClassName, appContext.classLoader)) {
            "CoApi [${effectiveDefinition.name}] is load balanced, but Spring Cloud LoadBalancer is not on the classpath. " +
                "Add `spring-cloud-starter-loadbalancer`, or disable it via " +
                "`coapi.clients.${effectiveDefinition.name}.load-balanced=false`."
        }
    }
}
