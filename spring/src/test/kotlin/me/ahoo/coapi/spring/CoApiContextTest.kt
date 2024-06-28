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

import io.mockk.mockk
import me.ahoo.coapi.example.consumer.client.GitHubApiClient
import me.ahoo.coapi.example.consumer.client.ServiceApiClient
import me.ahoo.coapi.example.consumer.client.ServiceApiClientUseFilterBeanName
import me.ahoo.coapi.example.consumer.client.ServiceApiClientUseFilterType
import me.ahoo.coapi.example.provider.client.TodoClient
import me.ahoo.coapi.spring.client.ClientProperties
import me.ahoo.coapi.spring.client.reactive.ReactiveHttpExchangeAdapterFactory
import me.ahoo.coapi.spring.client.reactive.WebClientBuilderCustomizer
import me.ahoo.coapi.spring.client.sync.RestClientBuilderCustomizer
import me.ahoo.coapi.spring.client.sync.SyncHttpExchangeAdapterFactory
import org.assertj.core.api.AssertionsForInterfaceTypes
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction
import org.springframework.web.client.RestClient

class CoApiContextTest {
    private val loadBalancedExchangeFilterName =
        ClientProperties.FilterDefinition(names = listOf("loadBalancerExchangeFilterFunction"))
    private val loadBalancedExchangeFilterType =
        ClientProperties.FilterDefinition(types = listOf(LoadBalancedExchangeFilterFunction::class.java))
    private val loadBalancedExchangeInterceptorName =
        ClientProperties.InterceptorDefinition(names = listOf("loadBalancerInterceptor"))
    private val loadBalancedExchangeInterceptorType =
        ClientProperties.InterceptorDefinition(types = listOf(LoadBalancerInterceptor::class.java))

    @Test
    fun `should create Reactive CoApi bean`() {
        ApplicationContextRunner()
            .withPropertyValues("github.url=https://api.github.com")
            .withBean("loadBalancerExchangeFilterFunction", LoadBalancedExchangeFilterFunction::class.java, { mockk() })
            .withBean(WebClientBuilderCustomizer::class.java, { WebClientBuilderCustomizer.NoOp })
            .withBean("clientProperties", ClientProperties::class.java, {
                MockClientProperties(
                    filter = mapOf(
                        "ServiceApiClientUseFilterBeanName" to loadBalancedExchangeFilterName,
                        "ServiceApiClientUseFilterType" to loadBalancedExchangeFilterType
                    )
                )
            })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(EnableCoApiConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasSingleBean(ReactiveHttpExchangeAdapterFactory::class.java)
                    .hasSingleBean(GitHubApiClient::class.java)
                    .hasSingleBean(ServiceApiClient::class.java)

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
            .withBean("loadBalancerInterceptor", LoadBalancerInterceptor::class.java, { mockk() })
            .withBean(RestClientBuilderCustomizer::class.java, { RestClientBuilderCustomizer.NoOp })
            .withBean(RestClient.Builder::class.java, {
                RestClient.builder()
            })
            .withBean("clientProperties", ClientProperties::class.java, {
                MockClientProperties(
                    interceptor = mapOf(
                        "ServiceApiClientUseFilterBeanName" to loadBalancedExchangeInterceptorName,
                        "ServiceApiClientUseFilterType" to loadBalancedExchangeInterceptorType
                    )
                )
            })
            .withUserConfiguration(EnableCoApiConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasSingleBean(SyncHttpExchangeAdapterFactory::class.java)
                    .hasSingleBean(GitHubApiClient::class.java)
                    .hasSingleBean(ServiceApiClient::class.java)

                context.getBean(GitHubApiClient::class.java)
                context.getBean(ServiceApiClient::class.java)
                context.getBean(ServiceApiClientUseFilterBeanName::class.java)
                context.getBean(ServiceApiClientUseFilterType::class.java)
            }
    }
}

@EnableCoApi(
    clients = [
        GitHubApiClient::class,
        ServiceApiClient::class,
        ServiceApiClientUseFilterBeanName::class,
        ServiceApiClientUseFilterType::class,
        TodoClient::class
    ]
)
class EnableCoApiConfiguration

data class MockClientProperties(
    val filter: Map<String, ClientProperties.FilterDefinition> = emptyMap(),
    val interceptor: Map<String, ClientProperties.InterceptorDefinition> = emptyMap(),
) : ClientProperties {
    override fun getBaseUri(coApiName: String): String {
        return ""
    }

    override fun getFilter(coApiName: String): ClientProperties.FilterDefinition {
        return filter[coApiName] ?: ClientProperties.FilterDefinition()
    }

    override fun getInterceptor(coApiName: String): ClientProperties.InterceptorDefinition {
        return interceptor[coApiName] ?: ClientProperties.InterceptorDefinition()
    }
}
