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

import io.mockk.mockk
import me.ahoo.coapi.example.consumer.client.GitHubApiClient
import me.ahoo.coapi.example.consumer.client.ServiceApiClient
import me.ahoo.coapi.example.consumer.client.ServiceApiClientUseFilterBeanName
import me.ahoo.coapi.example.consumer.client.ServiceApiClientUseFilterType
import me.ahoo.coapi.example.provider.client.TodoClient
import me.ahoo.coapi.spring.ClientMode
import me.ahoo.coapi.spring.CoApiDefinition
import me.ahoo.coapi.spring.EnableCoApi
import me.ahoo.coapi.spring.boot.definitions.InstanceDefinitionConfiguration
import me.ahoo.coapi.spring.boot.definitions.StaticDefinitionConfiguration
import me.ahoo.coapi.spring.client.reactive.ReactiveHttpExchangeAdapterFactory
import me.ahoo.coapi.spring.client.reactive.WebClientFactoryBean
import me.ahoo.coapi.spring.client.sync.SyncHttpExchangeAdapterFactory
import me.ahoo.test.asserts.assert
import org.assertj.core.api.AssertionsForInterfaceTypes
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction

@ExtendWith(OutputCaptureExtension::class)
class CoApiAutoConfigurationTest {
    private val filterNamePropertyK = "coapi.clients.ServiceApiClientUseFilterBeanName.reactive.filter.names"
    private val filterNameProperty = "$filterNamePropertyK=loadBalancerExchangeFilterFunction"
    private val filterTypePropertyK = "coapi.clients.ServiceApiClientUseFilterType.reactive.filter.types"
    private val filterTypeProperty =
        "$filterTypePropertyK=org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction"

    private val interceptorNamePropertyK = "coapi.clients.ServiceApiClientUseFilterBeanName.sync.interceptor.names"
    private val interceptorNameProperty = "$interceptorNamePropertyK=loadBalancerInterceptor"
    private val interceptorTypePropertyK = "coapi.clients.ServiceApiClientUseFilterType.sync.interceptor.types"
    private val interceptorTypeProperty =
        "$interceptorTypePropertyK=org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor"

    @Test
    fun `should create Reactive CoApi bean`() {
        ApplicationContextRunner()
            .withPropertyValues("github.url=https://api.github.com")
            .withPropertyValues(filterNameProperty)
            .withPropertyValues(filterTypeProperty)
            .withBean("loadBalancerExchangeFilterFunction", LoadBalancedExchangeFilterFunction::class.java, { mockk() })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(EnableCoApiConfiguration::class.java)
            .withUserConfiguration(CoApiAutoConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasSingleBean(ReactiveHttpExchangeAdapterFactory::class.java)
                    .hasSingleBean(GitHubApiClient::class.java)
                    .hasSingleBean(ServiceApiClient::class.java)
                val coApiProperties = context.getBean(CoApiProperties::class.java)
                coApiProperties.mode.assert().isEqualTo(ClientMode.AUTO)
                coApiProperties.clients["ServiceApiClientUseFilterBeanName"].assert().isNotNull()
                context.getBean<GitHubApiClient>()
                context.getBean<ServiceApiClient>()
                context.getBean<ServiceApiClientUseFilterBeanName>()
                context.getBean<ServiceApiClientUseFilterType>()
            }
    }

    @Test
    fun `should create Sync CoApi bean`() {
        ApplicationContextRunner()
            .withPropertyValues("${ClientMode.COAPI_CLIENT_MODE_PROPERTY}=SYNC")
            .withPropertyValues("github.url=https://api.github.com")
            .withPropertyValues(interceptorNameProperty)
            .withPropertyValues(interceptorTypeProperty)
            .withBean("loadBalancerInterceptor", LoadBalancerInterceptor::class.java, { mockk() })
            .withUserConfiguration(RestClientAutoConfiguration::class.java)
            .withUserConfiguration(EnableCoApiConfiguration::class.java)
            .withUserConfiguration(CoApiAutoConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasSingleBean(SyncHttpExchangeAdapterFactory::class.java)
                    .hasSingleBean(GitHubApiClient::class.java)
                    .hasSingleBean(ServiceApiClient::class.java)
                val coApiProperties = context.getBean<CoApiProperties>()
                coApiProperties.mode.assert().isEqualTo(ClientMode.SYNC)
                context.getBean<GitHubApiClient>()
                context.getBean<ServiceApiClient>()
                context.getBean<ServiceApiClientUseFilterBeanName>()
                context.getBean<ServiceApiClientUseFilterType>()
            }
    }

    @Test
    fun `loadBalanced property should override annotation and disable load balancing`() {
        ApplicationContextRunner()
            .withPropertyValues("coapi.clients.GitHubApi.load-balanced=false")
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(CoApiAutoConfiguration::class.java)
            .withUserConfiguration(ServiceApiOnlyConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasSingleBean(ServiceApiClient::class.java)
                context.getBean<ServiceApiClient>()
            }
    }

    @Test
    fun `duplicate name across scan and definition bean should fail startup`() {
        ApplicationContextRunner()
            .withPropertyValues("github.url=https://api.github.com")
            .withPropertyValues("coapi.base-packages=me.ahoo.coapi.example.consumer.client")
            .withBean("conflictingDefinition", CoApiDefinition::class.java, {
                CoApiDefinition(
                    name = "GitHubApi",
                    apiType = Any::class.java,
                    baseUrl = "http://conflict",
                    loadBalanced = false
                )
            })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(CoApiAutoConfiguration::class.java)
            .run { context ->
                val failure = requireNotNull(context.startupFailure)
                failure.toString().assert().contains("Duplicate CoApi name [GitHubApi]")
                failure.toString().assert().contains(ServiceApiClient::class.java.name)
                failure.toString().assert().contains(Any::class.java.name)
            }
    }

    @Test
    fun `static CoApiDefinition bean method should not initialize its configuration early`(output: CapturedOutput) {
        ApplicationContextRunner()
            .withPropertyValues("static.url=http://static-definition")
            .withUserConfiguration(StaticDefinitionConfiguration::class.java)
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(CoApiAutoConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasNotFailed()
                    .hasBean("StaticDefinition.CoApi")
                val configuration = context.getBean<StaticDefinitionConfiguration>()
                // Field injection only happens when the configuration is created after bean post-processing
                configuration.url.assert().isEqualTo("http://static-definition")
                context.getBean<CoApiDefinition>().baseUrl.assert().isEqualTo("http://static-definition")
            }
        output.out.assert().doesNotContain("created too early")
    }

    @Test
    fun `instance CoApiDefinition bean method should fail startup`() {
        ApplicationContextRunner()
            .withUserConfiguration(InstanceDefinitionConfiguration::class.java)
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(CoApiAutoConfiguration::class.java)
            .run { context ->
                val failure = requireNotNull(context.startupFailure)
                failure.toString().assert()
                    .contains("CoApiDefinition bean [instanceDefinition] is declared by a non-static @Bean method")
            }
    }

    @Test
    fun `placeholders and lb scheme in CoApiDefinition beans should be resolved`() {
        ApplicationContextRunner()
            .withPropertyValues("order.service-id=order-service")
            .withBean("placeholderDefinition", CoApiDefinition::class.java, {
                CoApiDefinition(
                    name = "PlaceholderDefinition",
                    apiType = ServiceApiClient::class.java,
                    baseUrl = "lb://\${order.service-id}",
                    loadBalanced = false
                )
            })
            .withBean("loadBalancerExchangeFilterFunction", LoadBalancedExchangeFilterFunction::class.java, { mockk() })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(CoApiAutoConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context).hasNotFailed()
                val factoryBean = context.getBean<WebClientFactoryBean>("&PlaceholderDefinition.HttpClient")
                factoryBean.definition.baseUrl.assert().isEqualTo("http://order-service")
                factoryBean.definition.loadBalanced.assert().isTrue()
            }
    }

    @Test
    fun `scanned CoApi interfaces should be loaded with the bean class loader`() {
        // e.g. Spring Boot DevTools' RestartClassLoader: loading with CoApi's own class loader would
        // produce proxy types that do not match the application's injection points.
        val beanClassLoader = RecordingClassLoader(javaClass.classLoader)
        ApplicationContextRunner()
            .withClassLoader(beanClassLoader)
            .withPropertyValues("github.url=https://api.github.com")
            .withPropertyValues("coapi.base-packages=me.ahoo.coapi.example.consumer.client")
            .withBean("loadBalancerExchangeFilterFunction", LoadBalancedExchangeFilterFunction::class.java, { mockk() })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(CoApiAutoConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context).hasNotFailed()
                beanClassLoader.loadedClassNames.assert().contains(GitHubApiClient::class.java.name)
            }
    }

    private class RecordingClassLoader(parent: ClassLoader) : ClassLoader(parent) {
        val loadedClassNames: MutableSet<String> = java.util.concurrent.ConcurrentHashMap.newKeySet()

        override fun loadClass(name: String, resolve: Boolean): Class<*> {
            loadedClassNames += name
            return super.loadClass(name, resolve)
        }
    }

    @Test
    fun basePackages() {
        ApplicationContextRunner()
            .withPropertyValues("github.url=https://api.github.com")
            .withPropertyValues("${CoApiProperties.COAPI_BASE_PACKAGES}=me.ahoo.coapi.spring.boot.starter")
            .withPropertyValues(filterNameProperty)
            .withPropertyValues(filterTypeProperty)
            .withBean("loadBalancerExchangeFilterFunction", LoadBalancedExchangeFilterFunction::class.java, { mockk() })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(CoApiAutoConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasSingleBean(ReactiveHttpExchangeAdapterFactory::class.java)
                    .hasSingleBean(me.ahoo.coapi.spring.boot.starter.GitHubApiClient::class.java)
            }
    }

    @Test
    fun basePackagesMultiple() {
        ApplicationContextRunner()
            .withPropertyValues("github.url=https://api.github.com")
            .withPropertyValues(
                // whitespace after the comma must be tolerated
                "${CoApiProperties.COAPI_BASE_PACKAGES}=me.ahoo.coapi.spring.boot.starter" +
                    ", me.ahoo.coapi.example.consumer.client"
            )
            .withPropertyValues(filterNameProperty)
            .withPropertyValues(filterTypeProperty)
            .withBean("loadBalancerExchangeFilterFunction", LoadBalancedExchangeFilterFunction::class.java, { mockk() })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(CoApiAutoConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasSingleBean(ReactiveHttpExchangeAdapterFactory::class.java)
                    .hasSingleBean(me.ahoo.coapi.spring.boot.starter.GitHubApiClient::class.java)
                    .hasSingleBean(ServiceApiClient::class.java)
            }
    }

    @Test
    fun basePackagesYaml() {
        ApplicationContextRunner()
            .withPropertyValues("github.url=https://api.github.com")
            .withPropertyValues(
                "${CoApiProperties.COAPI_BASE_PACKAGES}[0]=me.ahoo.coapi.spring.boot.starter"
            )
            .withPropertyValues(
                "${CoApiProperties.COAPI_BASE_PACKAGES}[1]=me.ahoo.coapi.example.consumer.client"
            )
            .withPropertyValues(filterNameProperty)
            .withPropertyValues(filterTypeProperty)
            .withBean("loadBalancerExchangeFilterFunction", LoadBalancedExchangeFilterFunction::class.java, { mockk() })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(CoApiAutoConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasSingleBean(ReactiveHttpExchangeAdapterFactory::class.java)
                    .hasSingleBean(me.ahoo.coapi.spring.boot.starter.GitHubApiClient::class.java)
                    .hasSingleBean(ServiceApiClient::class.java)
            }
    }
}

@AutoConfigurationPackage(
    basePackageClasses = [GitHubApiClient::class]
)
@SpringBootApplication
@EnableCoApi(
    clients = [
        GitHubApiClient::class,
        ServiceApiClient::class,
        ServiceApiClientUseFilterBeanName::class,
        ServiceApiClientUseFilterType::class,
        TodoClient::class,
        me.ahoo.coapi.spring.boot.starter.GitHubApiClient::class
    ]
)
class EnableCoApiConfiguration

@EnableCoApi(clients = [ServiceApiClient::class])
class ServiceApiOnlyConfiguration
