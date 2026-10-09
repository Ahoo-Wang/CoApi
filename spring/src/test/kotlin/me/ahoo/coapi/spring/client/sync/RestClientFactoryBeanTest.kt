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

package me.ahoo.coapi.spring.client.sync

import io.mockk.every
import io.mockk.mockk
import me.ahoo.coapi.spring.CoApiDefinition
import me.ahoo.test.asserts.assert
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.BeanFactory
import org.springframework.cloud.client.loadbalancer.BlockingLoadBalancerInterceptor
import org.springframework.cloud.client.loadbalancer.DeferringLoadBalancerInterceptor
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient

class RestClientFactoryBeanTest {

    private val definition = CoApiDefinition(
        name = "testClient",
        apiType = Any::class.java,
        baseUrl = "http://localhost:8080",
        loadBalanced = true
    )

    private fun RestClient.Builder.interceptors(): List<ClientHttpRequestInterceptor> {
        var interceptors: List<ClientHttpRequestInterceptor> = emptyList()
        requestInterceptors { interceptors = it.toList() }
        return interceptors
    }

    @Test
    fun `should not add duplicate load balancer interceptor when already present`() {
        val existingInterceptor = mockk<LoadBalancerInterceptor>()
        val builder = RestClient.builder().requestInterceptor(existingInterceptor)

        LoadBalancedRestClientBuilderCustomizer(mockk()).customize(definition, builder)

        builder.interceptors().assert().containsExactly(existingInterceptor)
    }

    @Test
    fun `should not add duplicate load balancer interceptor when deferring interceptor already present`() {
        val existingInterceptor = mockk<DeferringLoadBalancerInterceptor>()
        val builder = RestClient.builder().requestInterceptor(existingInterceptor)

        LoadBalancedRestClientBuilderCustomizer(mockk()).customize(definition, builder)

        builder.interceptors().assert().containsExactly(existingInterceptor)
    }

    @Test
    fun `should add single load balancer interceptor when none present`() {
        val loadBalancerInterceptor = mockk<BlockingLoadBalancerInterceptor>()
        val beanFactory = mockk<BeanFactory>()
        every { beanFactory.getBean(BlockingLoadBalancerInterceptor::class.java) } returns loadBalancerInterceptor
        val builder = RestClient.builder()

        LoadBalancedRestClientBuilderCustomizer(beanFactory).customize(definition, builder)

        builder.interceptors().assert().containsExactly(loadBalancerInterceptor)
    }
}
