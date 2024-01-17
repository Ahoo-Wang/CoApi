package me.ahoo.coapi.spring

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry

class CoApiRegistrar(private val registry: BeanDefinitionRegistry) {
    companion object {
        private val log = LoggerFactory.getLogger(CoApiRegistrar::class.java)
    }

    fun register(coApiDefinitions: Set<CoApiDefinition>) {
        coApiDefinitions.forEach {
            register(it)
        }
    }

    fun register(coApiDefinition: CoApiDefinition) {
        registerWebClient(registry, coApiDefinition)
        registerApiClient(registry, coApiDefinition)
    }

    private fun registerWebClient(registry: BeanDefinitionRegistry, coApiDefinition: CoApiDefinition) {
        if (log.isInfoEnabled) {
            log.info("Register WebClient [{}].", coApiDefinition.webClientBeanName)
        }
        if (registry.containsBeanDefinition(coApiDefinition.webClientBeanName)) {
            if (log.isWarnEnabled) {
                log.warn(
                    "WebClient [{}] already exists - Ignore.",
                    coApiDefinition.webClientBeanName
                )
            }
            return
        }
        val beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebClientFactoryBean::class.java)
        beanDefinitionBuilder.addConstructorArgValue(coApiDefinition)
        registry.registerBeanDefinition(coApiDefinition.webClientBeanName, beanDefinitionBuilder.beanDefinition)
    }

    private fun registerApiClient(registry: BeanDefinitionRegistry, coApiDefinition: CoApiDefinition) {
        if (log.isInfoEnabled) {
            log.info("Register CoApi [{}].", coApiDefinition.coApiBeanName)
        }
        if (registry.containsBeanDefinition(coApiDefinition.coApiBeanName)) {
            if (log.isWarnEnabled) {
                log.warn(
                    "CoApi [{}] already exists - Ignore.",
                    coApiDefinition.coApiBeanName
                )
            }
            return
        }
        val beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(CoApiFactoryBean::class.java)
        beanDefinitionBuilder.addConstructorArgValue(coApiDefinition)
        registry.registerBeanDefinition(coApiDefinition.coApiBeanName, beanDefinitionBuilder.beanDefinition)
    }
}
