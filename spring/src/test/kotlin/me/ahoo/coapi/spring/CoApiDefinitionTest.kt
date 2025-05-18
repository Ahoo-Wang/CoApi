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

import me.ahoo.coapi.api.CoApi
import me.ahoo.coapi.api.LoadBalanced
import me.ahoo.coapi.spring.CoApiDefinition.Companion.toCoApiDefinition
import me.ahoo.test.asserts.assert
import me.ahoo.test.asserts.assertThrownBy
import org.junit.jupiter.api.Test
import org.springframework.mock.env.MockEnvironment

class CoApiDefinitionTest {

    @Test
    fun toCoApiDefinitionIfNoCoApi() {
        assertThrownBy<IllegalArgumentException> {
            CoApiDefinitionTest::class.java.toCoApiDefinition(MockEnvironment())
        }
    }

    @Test
    fun toCoApiDefinitionIfNoLBMockApi() {
        val coApiDefinition = LBMockApi::class.java.toCoApiDefinition(MockEnvironment())
        coApiDefinition.loadBalanced.assert().isTrue()
        coApiDefinition.baseUrl.assert().isEqualTo("http://order-service")
    }

    @Test
    fun toCoApiDefinitionIfServiceApi() {
        val coApiDefinition = MockServiceApi::class.java.toCoApiDefinition(MockEnvironment())
        coApiDefinition.loadBalanced.assert().isTrue()
        coApiDefinition.baseUrl.assert().isEqualTo("http://order-service")
    }

    @Test
    fun toCoApiDefinitionIfEmptyApi() {
        val coApiDefinition = MockEmptyApi::class.java.toCoApiDefinition(MockEnvironment())
        coApiDefinition.loadBalanced.assert().isTrue()
        coApiDefinition.baseUrl.assert().isEmpty()
    }
}

@CoApi(baseUrl = "lb://order-service")
interface LBMockApi

@CoApi(serviceId = "order-service")
interface MockServiceApi

@CoApi
@LoadBalanced
interface MockEmptyApi
