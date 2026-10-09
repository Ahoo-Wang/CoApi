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
import me.ahoo.test.asserts.assert
import me.ahoo.test.asserts.assertThrownBy
import org.junit.jupiter.api.Test
import org.springframework.context.support.StaticApplicationContext
import org.springframework.web.client.RestClient

class ClientPropertiesTest {

    private val definition = CoApiDefinition(
        name = "testClient",
        apiType = Any::class.java,
        baseUrl = "http://localhost:8080",
        loadBalanced = false
    )

    private fun properties(baseUrl: String = "", loadBalanced: Boolean? = null) = object : ClientProperties {
        override fun getBaseUri(coApiName: String): String = baseUrl
        override fun getLoadBalanced(coApiName: String): Boolean? = loadBalanced
    }

    @Test
    fun `no overrides should keep the definition`() {
        properties().resolve(definition).assert().isEqualTo(definition)
        ClientProperties.Empty.resolve(definition).assert().isEqualTo(definition)
    }

    @Test
    fun `configured base URL should win and disable load balancing by default`() {
        val resolved = properties(baseUrl = "http://properties-url:9090")
            .resolve(definition.copy(loadBalanced = true))
        resolved.baseUrl.assert().isEqualTo("http://properties-url:9090")
        resolved.loadBalanced.assert().isFalse()
    }

    @Test
    fun `lb scheme in configured base URL should be rewritten and imply load balancing`() {
        val resolved = properties(baseUrl = "lb://order-service").resolve(definition)
        resolved.baseUrl.assert().isEqualTo("http://order-service")
        resolved.loadBalanced.assert().isTrue()
    }

    @Test
    fun `explicit load balanced override should always win`() {
        properties(loadBalanced = true).resolve(definition).loadBalanced.assert().isTrue()
        properties(loadBalanced = false).resolve(definition.copy(loadBalanced = true)).loadBalanced.assert().isFalse()
        properties(baseUrl = "lb://order-service", loadBalanced = false).resolve(definition)
            .loadBalanced.assert().isFalse()
    }

    @Test
    fun `explicit false should win over a non-normalized lb definition`() {
        val lbDefinition = definition.copy(baseUrl = "lb://order-service")
        val resolved = properties(loadBalanced = false).resolve(lbDefinition)
        resolved.baseUrl.assert().isEqualTo("http://order-service")
        resolved.loadBalanced.assert().isFalse()
    }

    @Test
    fun `factory bean should fall back to the definition without a ClientProperties bean`() {
        val factoryBean = object : AbstractHttpClientFactoryBean<RestClient>(definition) {
            override fun getObject(): RestClient = RestClient.create()
            override fun getObjectType(): Class<*> = RestClient::class.java
        }
        factoryBean.setApplicationContext(StaticApplicationContext())
        factoryBean.effectiveDefinition().assert().isEqualTo(definition)
    }

    @Test
    fun `load balanced client without Spring Cloud on the classpath should fail fast`() {
        val factoryBean = object : AbstractHttpClientFactoryBean<RestClient>(definition) {
            override fun getObject(): RestClient {
                requireLoadBalancerSupport(effectiveDefinition(), "org.example.MissingLoadBalancer")
                return RestClient.create()
            }

            override fun getObjectType(): Class<*> = RestClient::class.java
        }
        factoryBean.setApplicationContext(StaticApplicationContext())

        assertThrownBy<IllegalStateException> {
            factoryBean.getObject()
        }.hasMessageContaining("spring-cloud-starter-loadbalancer")
            .hasMessageContaining("coapi.clients.testClient.load-balanced=false")
    }
}
