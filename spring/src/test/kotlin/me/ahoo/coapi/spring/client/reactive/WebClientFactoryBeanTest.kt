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

package me.ahoo.coapi.spring.client.reactive

import io.mockk.every
import io.mockk.mockk
import me.ahoo.coapi.spring.CoApiDefinition
import me.ahoo.coapi.spring.client.ClientProperties
import me.ahoo.test.asserts.assert
import org.junit.jupiter.api.Test
import org.springframework.cloud.client.loadbalancer.reactive.DeferringLoadBalancerExchangeFilterFunction
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction
import org.springframework.context.ApplicationContext
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

class WebClientFactoryBeanTest {

    private val mockDefinition = CoApiDefinition(
        name = "testClient",
        apiType = Any::class.java,
        baseUrl = "http://localhost:8080",
        loadBalanced = true
    )

    @Test
    fun `customize should not add duplicate load balancer filter when deferring filter already present`() {
        val mockApplicationContext = mockk<ApplicationContext>()
        val mockClientProperties = mockk<ClientProperties>()
        every { mockApplicationContext.getBean(ClientProperties::class.java) } returns mockClientProperties
        every { mockClientProperties.getLoadBalanced("testClient") } returns true

        val existingFilter = mockk<DeferringLoadBalancerExchangeFilterFunction<ExchangeFilterFunction>>()
        val builder = WebClient.builder().filter(existingFilter)

        val factoryBean = WebClientFactoryBean(mockDefinition)
        factoryBean.setApplicationContext(mockApplicationContext)
        factoryBean.LoadBalancedWebClientBuilderCustomizer().customize(mockDefinition, builder)

        var filters: List<ExchangeFilterFunction> = emptyList()
        builder.filters { filters = it }
        filters.size.assert().isEqualTo(1)
        filters.first().assert().isSameAs(existingFilter)
    }

    @Test
    fun `customize should add single load balancer filter when none present`() {
        val mockApplicationContext = mockk<ApplicationContext>()
        val mockClientProperties = mockk<ClientProperties>()
        val loadBalancedFilter = mockk<LoadBalancedExchangeFilterFunction>()
        every { mockApplicationContext.getBean(ClientProperties::class.java) } returns mockClientProperties
        every { mockClientProperties.getLoadBalanced("testClient") } returns true
        every { mockApplicationContext.getBean(LoadBalancedExchangeFilterFunction::class.java) } returns loadBalancedFilter

        val builder = WebClient.builder()

        val factoryBean = WebClientFactoryBean(mockDefinition)
        factoryBean.setApplicationContext(mockApplicationContext)
        factoryBean.LoadBalancedWebClientBuilderCustomizer().customize(mockDefinition, builder)

        var filters: List<ExchangeFilterFunction> = emptyList()
        builder.filters { filters = it }
        filters.size.assert().isEqualTo(1)
        filters.first().assert().isSameAs(loadBalancedFilter)
    }
}
