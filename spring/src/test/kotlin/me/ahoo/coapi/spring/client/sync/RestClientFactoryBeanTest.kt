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
import me.ahoo.coapi.spring.client.ClientProperties
import me.ahoo.test.asserts.assert
import org.junit.jupiter.api.Test
import org.springframework.cloud.client.loadbalancer.BlockingLoadBalancerInterceptor
import org.springframework.cloud.client.loadbalancer.DeferringLoadBalancerInterceptor
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor
import org.springframework.context.ApplicationContext
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient

class RestClientFactoryBeanTest {

    private val mockDefinition = CoApiDefinition(
        name = "testClient",
        apiType = Any::class.java,
        baseUrl = "http://localhost:8080",
        loadBalanced = true
    )

    @Test
    fun `customize should not add duplicate load balancer interceptor when already present`() {
        val mockApplicationContext = mockk<ApplicationContext>()
        val mockClientProperties = mockk<ClientProperties>()
        every { mockApplicationContext.getBean(ClientProperties::class.java) } returns mockClientProperties
        every { mockClientProperties.getLoadBalanced("testClient") } returns true

        val existingInterceptor = mockk<LoadBalancerInterceptor>()
        val builder = RestClient.builder().requestInterceptor(existingInterceptor)

        val factoryBean = RestClientFactoryBean(mockDefinition)
        factoryBean.setApplicationContext(mockApplicationContext)
        factoryBean.LoadBalancedRestClientBuilderCustomizer().customize(mockDefinition, builder)

        var interceptors: List<ClientHttpRequestInterceptor> = emptyList()
        builder.requestInterceptors { interceptors = it }
        interceptors.size.assert().isEqualTo(1)
        interceptors.first().assert().isSameAs(existingInterceptor)
    }

    @Test
    fun `customize should not add duplicate load balancer interceptor when deferring interceptor already present`() {
        val mockApplicationContext = mockk<ApplicationContext>()
        val mockClientProperties = mockk<ClientProperties>()
        every { mockApplicationContext.getBean(ClientProperties::class.java) } returns mockClientProperties
        every { mockClientProperties.getLoadBalanced("testClient") } returns true

        val existingInterceptor = mockk<DeferringLoadBalancerInterceptor>()
        val builder = RestClient.builder().requestInterceptor(existingInterceptor)

        val factoryBean = RestClientFactoryBean(mockDefinition)
        factoryBean.setApplicationContext(mockApplicationContext)
        factoryBean.LoadBalancedRestClientBuilderCustomizer().customize(mockDefinition, builder)

        var interceptors: List<ClientHttpRequestInterceptor> = emptyList()
        builder.requestInterceptors { interceptors = it }
        interceptors.size.assert().isEqualTo(1)
        interceptors.first().assert().isSameAs(existingInterceptor)
    }

    @Test
    fun `customize should add single load balancer interceptor when none present`() {
        val mockApplicationContext = mockk<ApplicationContext>()
        val mockClientProperties = mockk<ClientProperties>()
        val loadBalancerInterceptor = mockk<BlockingLoadBalancerInterceptor>()
        every { mockApplicationContext.getBean(ClientProperties::class.java) } returns mockClientProperties
        every { mockClientProperties.getLoadBalanced("testClient") } returns true
        every { mockApplicationContext.getBean(BlockingLoadBalancerInterceptor::class.java) } returns loadBalancerInterceptor

        val builder = RestClient.builder()

        val factoryBean = RestClientFactoryBean(mockDefinition)
        factoryBean.setApplicationContext(mockApplicationContext)
        factoryBean.LoadBalancedRestClientBuilderCustomizer().customize(mockDefinition, builder)

        var interceptors: List<ClientHttpRequestInterceptor> = emptyList()
        builder.requestInterceptors { interceptors = it }
        interceptors.size.assert().isEqualTo(1)
        interceptors.first().assert().isSameAs(loadBalancerInterceptor)
    }
}
