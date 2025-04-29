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

import me.ahoo.coapi.spring.client.ClientProperties
import me.ahoo.test.asserts.assert
import org.junit.jupiter.api.Test

class CoApiPropertiesTest {

    @Test
    fun getEnabled() {
        val properties = CoApiProperties()
        properties.enabled.assert().isTrue()
    }

    @Test
    fun setEnabled() {
        val properties = CoApiProperties(false)
        properties.enabled = true
        properties.enabled.assert().isTrue()
    }

    @Test
    fun getBaseUrl() {
        val properties = CoApiProperties()
        properties.getBaseUri("").assert().isBlank()
    }

    @Test
    fun setBaseUrl() {
        val properties = CoApiProperties(
            clients = mapOf(
                "test" to ClientDefinition(
                    baseUrl = "test"
                )
            )
        )
        properties.getBaseUri("test").assert().isEqualTo("test")
    }

    @Test
    fun getBasePackages() {
        val properties = CoApiProperties()
        properties.basePackages.assert().isEmpty()
    }

    @Test
    fun getFilterIfDefault() {
        val properties = CoApiProperties()
        properties.getFilter("test").names.assert().isEmpty()
        properties.getFilter("test").types.assert().isEmpty()
    }

    @Test
    fun getFilter() {
        val properties = CoApiProperties(
            clients = mutableMapOf(
                "test" to ClientDefinition(
                    reactive = ReactiveClientDefinition(
                        filter = ClientProperties.FilterDefinition(
                            listOf("test")
                        )
                    )
                )
            )
        )
        properties.getFilter("test").names.assert().hasSize(1)
        properties.getFilter("test").types.assert().isEmpty()
    }

    @Test
    fun getInterceptor() {
        val properties = CoApiProperties()
        properties.getInterceptor("test").names.assert().isEmpty()
        properties.getInterceptor("test").types.assert().isEmpty()
    }

    @Test
    fun setClientDefinition() {
        val properties = ClientDefinition()
        val reactive = ReactiveClientDefinition()
        properties.reactive = reactive
        properties.reactive.assert().isSameAs(reactive)
        val sync = SyncClientDefinition()
        properties.sync = sync
        properties.sync.assert().isSameAs(sync)
    }

    @Test
    fun setReactiveClientDefinition() {
        val properties = ReactiveClientDefinition()
        val filter = ClientProperties.FilterDefinition()
        properties.filter = filter
        properties.filter.assert().isSameAs(filter)
    }

    @Test
    fun setSyncClientDefinition() {
        val properties = SyncClientDefinition()
        val interceptor = ClientProperties.InterceptorDefinition()
        properties.interceptor = interceptor
        properties.interceptor.assert().isSameAs(interceptor)
    }
}
