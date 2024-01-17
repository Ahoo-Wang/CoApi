package me.ahoo.coapi.spring

import me.ahoo.coapi.spring.CoApiDefinition.Companion.toCoApiDefinition
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotationMetadata

class EnableCoApiRegistrar : ImportBeanDefinitionRegistrar, EnvironmentAware {

    private lateinit var env: Environment

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val coApiRegistrar = CoApiRegistrar(registry)
        val apiClientDefinitions = importingClassMetadata.toApiClientDefinitions()
        coApiRegistrar.register(apiClientDefinitions)
    }

    @Suppress("UNCHECKED_CAST")
    private fun AnnotationMetadata.toApiClientDefinitions(): Set<CoApiDefinition> {
        val enableCoApi = getAnnotationAttributes(EnableCoApi::class.java.name) ?: return emptySet()
        val apis = enableCoApi[EnableCoApi::apis.name] as Array<Class<*>>
        return apis.map { clientType ->
            clientType.toCoApiDefinition(env)
        }.toSet()
    }

    override fun setEnvironment(environment: Environment) {
        this.env = environment
    }
}
