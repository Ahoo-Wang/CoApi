package me.ahoo.coapi.spring

import me.ahoo.coapi.spring.ApiClientDefinition.Companion.toApiClientDefinition
import org.springframework.core.type.AnnotationMetadata

class EnableApiClientsRegistrar : AbstractApiClientRegistrar() {

    @Suppress("UNCHECKED_CAST")
    override fun getApiClientDefinitions(importingClassMetadata: AnnotationMetadata): Set<ApiClientDefinition> {
        val enableApiClients =
            importingClassMetadata.getAnnotationAttributes(EnableApiClients::class.java.name) ?: return emptySet()
        val clients = enableApiClients[EnableApiClients::clients.name] as Array<Class<*>>
        return clients.map { clientType ->
            clientType.toApiClientDefinition(env)
        }.toSet()
    }
}
