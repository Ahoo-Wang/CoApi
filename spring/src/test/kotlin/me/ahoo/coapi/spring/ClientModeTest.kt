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

import me.ahoo.test.asserts.assert
import me.ahoo.test.asserts.assertThrownBy
import org.junit.jupiter.api.Test

class ClientModeTest {

    @Test
    fun inferClientModeIfNull() {
        val mode = ClientMode.inferClientMode {
            null
        }
        mode.assert().isEqualTo(ClientMode.REACTIVE)
    }

    @Test
    fun inferClientMode() {
        val mode = ClientMode.inferClientMode {
            "SYNC"
        }
        mode.assert().isEqualTo(ClientMode.SYNC)
    }

    @Test
    fun inferClientModeIfInvalid() {
        assertThrownBy<IllegalArgumentException> {
            ClientMode.inferClientMode { "FOO" }
        }.hasMessageContaining("FOO")
            .hasMessageContaining("REACTIVE, SYNC, AUTO")
            .hasCauseInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun resolveAutoShouldInferFromClasspath() {
        ClientMode.AUTO.resolve().assert().isEqualTo(ClientMode.REACTIVE)
    }

    @Test
    fun resolveConcreteModeShouldReturnItself() {
        ClientMode.SYNC.resolve().assert().isEqualTo(ClientMode.SYNC)
        ClientMode.REACTIVE.resolve().assert().isEqualTo(ClientMode.REACTIVE)
    }
}
