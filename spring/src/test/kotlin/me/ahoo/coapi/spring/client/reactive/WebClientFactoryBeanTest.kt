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
import me.ahoo.test.asserts.assert
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.BeanFactory
import org.springframework.cloud.client.loadbalancer.reactive.DeferringLoadBalancerExchangeFilterFunction
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

class WebClientFactoryBeanTest {

    private val definition = CoApiDefinition(
        name = "testClient",
        apiType = Any::class.java,
        baseUrl = "http://localhost:8080",
        loadBalanced = true
    )

    private fun WebClient.Builder.filters(): List<ExchangeFilterFunction> {
        var filters: List<ExchangeFilterFunction> = emptyList()
        filters { filters = it.toList() }
        return filters
    }

    @Test
    fun `should not add duplicate load balancer filter when deferring filter already present`() {
        val existingFilter = mockk<DeferringLoadBalancerExchangeFilterFunction<ExchangeFilterFunction>>()
        val builder = WebClient.builder().filter(existingFilter)

        LoadBalancedWebClientBuilderCustomizer(mockk()).customize(definition, builder)

        builder.filters().assert().containsExactly(existingFilter)
    }

    @Test
    fun `should add single load balancer filter when none present`() {
        val loadBalancedFilter = mockk<LoadBalancedExchangeFilterFunction>()
        val beanFactory = mockk<BeanFactory>()
        every { beanFactory.getBean(LoadBalancedExchangeFilterFunction::class.java) } returns loadBalancedFilter
        val builder = WebClient.builder()

        LoadBalancedWebClientBuilderCustomizer(beanFactory).customize(definition, builder)

        builder.filters().assert().containsExactly(loadBalancedFilter)
    }
}
