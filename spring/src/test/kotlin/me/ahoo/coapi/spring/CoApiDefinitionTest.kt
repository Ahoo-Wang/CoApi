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
