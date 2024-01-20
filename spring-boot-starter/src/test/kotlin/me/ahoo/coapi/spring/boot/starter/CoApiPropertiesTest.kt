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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.sameInstance
import org.junit.jupiter.api.Test

class CoApiPropertiesTest {

    @Test
    fun getEnabled() {
        val properties = CoApiProperties()
        assertThat(properties.enabled, equalTo(true))
    }

    @Test
    fun setEnabled() {
        val properties = CoApiProperties(false)
        properties.enabled = true
        assertThat(properties.enabled, equalTo(true))
    }

    @Test
    fun getBasePackages() {
        val properties = CoApiProperties()
        assertThat(properties.basePackages, Matchers.empty())
    }

    @Test
    fun getFilterIfDefault() {
        val properties = CoApiProperties()
        assertThat(properties.getFilter("test").names, Matchers.empty())
        assertThat(properties.getFilter("test").types, Matchers.empty())
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
        assertThat(properties.getFilter("test").names, Matchers.hasSize(1))
        assertThat(properties.getFilter("test").types, Matchers.empty())
    }

    @Test
    fun getInterceptor() {
        val properties = CoApiProperties()
        assertThat(properties.getInterceptor("test").names, Matchers.empty())
        assertThat(properties.getInterceptor("test").types, Matchers.empty())
    }

    @Test
    fun setClientDefinition() {
        val properties = ClientDefinition()
        val reactive = ReactiveClientDefinition()
        properties.reactive = reactive
        assertThat(properties.reactive, sameInstance(reactive))
        val sync = SyncClientDefinition()
        properties.sync = sync
        assertThat(properties.sync, sameInstance(sync))
    }

    @Test
    fun setReactiveClientDefinition() {
        val properties = ReactiveClientDefinition()
        val filter = ClientProperties.FilterDefinition()
        properties.filter = filter
        assertThat(properties.filter, sameInstance(filter))
    }

    @Test
    fun setSyncClientDefinition() {
        val properties = SyncClientDefinition()
        val interceptor = ClientProperties.InterceptorDefinition()
        properties.interceptor = interceptor
        assertThat(properties.interceptor, sameInstance(interceptor))
    }
}
