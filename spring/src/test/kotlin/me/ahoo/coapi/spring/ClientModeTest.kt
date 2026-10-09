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
