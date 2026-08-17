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

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import me.ahoo.test.asserts.assertThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.support.BeanDefinitionRegistry

class CoApiRegistrarTest {

    private fun mockRegistry(): BeanDefinitionRegistry {
        val registry = mockk<BeanDefinitionRegistry>()
        every { registry.containsBeanDefinition(any<String>()) } returns false
        every { registry.registerBeanDefinition(any(), any()) } just Runs
        return registry
    }

    @Test
    fun registerDuplicateNameShouldThrow() {
        val registry = mockRegistry()
        val registrar = CoApiRegistrar(registry, ClientMode.REACTIVE)
        val definition1 = CoApiDefinition("SameName", Any::class.java, "http://a", false)
        val definition2 = CoApiDefinition("SameName", String::class.java, "http://b", false)

        assertThrownBy<IllegalStateException> {
            registrar.register(setOf(definition1, definition2))
        }
    }

    @Test
    fun registerDistinctNamesShouldRegisterAll() {
        val registry = mockRegistry()
        val registrar = CoApiRegistrar(registry, ClientMode.REACTIVE)
        val definition1 = CoApiDefinition("ApiA", Any::class.java, "http://a", false)
        val definition2 = CoApiDefinition("ApiB", String::class.java, "http://b", false)

        registrar.register(setOf(definition1, definition2))

        verify(exactly = 4) { registry.registerBeanDefinition(any(), any()) }
    }
}
