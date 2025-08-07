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

import io.mockk.every
import io.mockk.mockk
import me.ahoo.coapi.spring.CoApiDefinition
import me.ahoo.test.asserts.assert
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationContext

class IHttpClientFactoryBeanTest {

    private val mockDefinition = CoApiDefinition(
        name = "testClient",
        apiType = Any::class.java,
        baseUrl = "http://localhost:8080",
        loadBalanced = false
    )

    private class TestHttpClientFactoryBean(
        override val definition: CoApiDefinition
    ) : AbstractHttpClientFactoryBean()

    @Test
    fun `getBaseUrlFromProperties should return base URL from properties`() {
        val mockApplicationContext = mockk<ApplicationContext>()
        val mockClientProperties = mockk<ClientProperties>()

        every { mockApplicationContext.getBean(ClientProperties::class.java) } returns mockClientProperties
        every { mockClientProperties.getBaseUri("testClient") } returns "http://properties-url:9090"

        val factoryBean = TestHttpClientFactoryBean(mockDefinition)
        factoryBean.setApplicationContext(mockApplicationContext)
        val baseUrl = factoryBean.getBaseUrlFromProperties()

        baseUrl.assert().isEqualTo("http://properties-url:9090")
    }

    @Test
    fun `getLoadBalancedFromProperties should return load balanced value from properties`() {
        val mockApplicationContext = mockk<ApplicationContext>()
        val mockClientProperties = mockk<ClientProperties>()

        every { mockApplicationContext.getBean(ClientProperties::class.java) } returns mockClientProperties
        every { mockClientProperties.getLoadBalanced("testClient") } returns true

        val factoryBean = TestHttpClientFactoryBean(mockDefinition)
        factoryBean.setApplicationContext(mockApplicationContext)
        val loadBalanced = factoryBean.getLoadBalancedFromProperties()

        loadBalanced.assert().isEqualTo(true)
    }

    @Test
    fun `getLoadBalancedFromProperties should return null when not configured`() {
        val mockApplicationContext = mockk<ApplicationContext>()
        val mockClientProperties = mockk<ClientProperties>()

        every { mockApplicationContext.getBean(ClientProperties::class.java) } returns mockClientProperties
        every { mockClientProperties.getLoadBalanced("testClient") } returns null

        val factoryBean = TestHttpClientFactoryBean(mockDefinition)
        factoryBean.setApplicationContext(mockApplicationContext)
        val loadBalanced = factoryBean.getLoadBalancedFromProperties()

        loadBalanced.assert().isNull()
    }

    @Test
    fun `getBaseUrl should return URL from properties when available`() {
        val mockApplicationContext = mockk<ApplicationContext>()
        val mockClientProperties = mockk<ClientProperties>()

        every { mockApplicationContext.getBean(ClientProperties::class.java) } returns mockClientProperties
        every { mockClientProperties.getBaseUri("testClient") } returns "http://properties-url:9090"

        val factoryBean = TestHttpClientFactoryBean(mockDefinition)
        factoryBean.setApplicationContext(mockApplicationContext)
        val baseUrl = factoryBean.getBaseUrl()

        baseUrl.assert().isEqualTo("http://properties-url:9090")
    }

    @Test
    fun `getBaseUrl should return default URL when properties URL is blank`() {
        val mockApplicationContext = mockk<ApplicationContext>()
        val mockClientProperties = mockk<ClientProperties>()

        every { mockApplicationContext.getBean(ClientProperties::class.java) } returns mockClientProperties
        every { mockClientProperties.getBaseUri("testClient") } returns ""

        val factoryBean = TestHttpClientFactoryBean(mockDefinition)
        factoryBean.setApplicationContext(mockApplicationContext)
        val baseUrl = factoryBean.getBaseUrl()

        baseUrl.assert().isEqualTo("http://localhost:8080")
    }

    @Test
    fun `loadBalanced should return true when properties explicitly set to true`() {
        val mockApplicationContext = mockk<ApplicationContext>()
        val mockClientProperties = mockk<ClientProperties>()

        every { mockApplicationContext.getBean(ClientProperties::class.java) } returns mockClientProperties
        every { mockClientProperties.getLoadBalanced("testClient") } returns true

        val factoryBean = TestHttpClientFactoryBean(mockDefinition)
        factoryBean.setApplicationContext(mockApplicationContext)
        val loadBalanced = factoryBean.loadBalanced()

        loadBalanced.assert().isEqualTo(true)
    }

    @Test
    fun `loadBalanced should return false when properties URL is not blank`() {
        val mockApplicationContext = mockk<ApplicationContext>()
        val mockClientProperties = mockk<ClientProperties>()

        every { mockApplicationContext.getBean(ClientProperties::class.java) } returns mockClientProperties
        every { mockClientProperties.getLoadBalanced("testClient") } returns null
        every { mockClientProperties.getBaseUri("testClient") } returns "http://example.com"

        val factoryBean = TestHttpClientFactoryBean(mockDefinition)
        factoryBean.setApplicationContext(mockApplicationContext)
        val loadBalanced = factoryBean.loadBalanced()

        loadBalanced.assert().isEqualTo(false)
    }

    @Test
    fun `loadBalanced should return default value when properties not configured`() {
        val mockApplicationContext = mockk<ApplicationContext>()
        val mockClientProperties = mockk<ClientProperties>()

        every { mockApplicationContext.getBean(ClientProperties::class.java) } returns mockClientProperties
        every { mockClientProperties.getLoadBalanced("testClient") } returns null
        every { mockClientProperties.getBaseUri("testClient") } returns ""

        val factoryBean = TestHttpClientFactoryBean(mockDefinition)
        factoryBean.setApplicationContext(mockApplicationContext)
        val loadBalanced = factoryBean.loadBalanced()

        loadBalanced.assert().isEqualTo(false)
    }
}
