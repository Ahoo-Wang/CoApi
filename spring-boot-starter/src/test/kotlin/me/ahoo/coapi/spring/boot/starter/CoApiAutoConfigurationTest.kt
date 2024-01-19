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
import me.ahoo.coapi.spring.EnableCoApi
import me.ahoo.coapi.spring.client.reactive.ReactiveHttpExchangeAdapterFactory
import me.ahoo.coapi.spring.client.sync.SyncHttpExchangeAdapterFactory
import org.assertj.core.api.AssertionsForInterfaceTypes
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction

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
                assertThat(coApiProperties.mode, equalTo(ClientMode.AUTO))
                assertThat(coApiProperties.clients["ServiceApiClientUseFilterBeanName"], notNullValue())
                context.getBean(GitHubApiClient::class.java)
                context.getBean(ServiceApiClient::class.java)
                context.getBean(ServiceApiClientUseFilterBeanName::class.java)
                context.getBean(ServiceApiClientUseFilterType::class.java)
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
            .withUserConfiguration(EnableCoApiConfiguration::class.java)
            .withUserConfiguration(CoApiAutoConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasSingleBean(SyncHttpExchangeAdapterFactory::class.java)
                    .hasSingleBean(GitHubApiClient::class.java)
                    .hasSingleBean(ServiceApiClient::class.java)
                val coApiProperties = context.getBean(CoApiProperties::class.java)
                assertThat(coApiProperties.mode, equalTo(ClientMode.SYNC))
                context.getBean(GitHubApiClient::class.java)
                context.getBean(ServiceApiClient::class.java)
                context.getBean(ServiceApiClientUseFilterBeanName::class.java)
                context.getBean(ServiceApiClientUseFilterType::class.java)
            }
    }
}

@SpringBootApplication
@EnableCoApi(
    apis = [
        GitHubApiClient::class,
        ServiceApiClient::class,
        ServiceApiClientUseFilterBeanName::class,
        ServiceApiClientUseFilterType::class,
        TodoClient::class,
        me.ahoo.coapi.spring.boot.starter.GitHubApiClient::class
    ]
)
class EnableCoApiConfiguration
