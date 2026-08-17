package me.ahoo.coapi.spring

import me.ahoo.test.asserts.assert
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
        val thrown = requireNotNull(
            try {
                ClientMode.inferClientMode { "FOO" }
                null
            } catch (e: IllegalArgumentException) {
                e
            }
        )
        thrown.message.assert().contains("FOO")
        thrown.message.assert().contains("REACTIVE, SYNC, AUTO")
        thrown.cause.assert().isInstanceOf(IllegalArgumentException::class.java)
    }
}
