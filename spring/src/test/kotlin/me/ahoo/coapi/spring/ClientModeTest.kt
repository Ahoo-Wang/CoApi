package me.ahoo.coapi.spring

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class ClientModeTest {

    @Test
    fun inferClientModeIfNull() {
        val mode = ClientMode.inferClientMode {
            null
        }

        assertThat(mode, equalTo(ClientMode.REACTIVE))
    }

    @Test
    fun inferClientMode() {
        val mode = ClientMode.inferClientMode {
            "SYNC"
        }

        assertThat(mode, equalTo(ClientMode.SYNC))
    }
}
