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

import io.mockk.mockk
import me.ahoo.coapi.api.CoApi
import me.ahoo.coapi.spring.CoApiDefinition.Companion.toCoApiDefinition
import org.junit.jupiter.api.Test

class CoApiDefinitionTest {

    @Test
    fun toCoApiDefinitionIfNoCoApi() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            CoApiDefinitionTest::class.java.toCoApiDefinition(mockk())
        }
    }

    @Test
    fun toCoApiDefinitionIfNoCoApiValue() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            NoDefCoApi::class.java.toCoApiDefinition(mockk())
        }
    }
}

@CoApi
interface NoDefCoApi
