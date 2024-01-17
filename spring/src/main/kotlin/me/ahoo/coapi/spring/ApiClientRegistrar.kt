package me.ahoo.coapi.spring

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry

class ApiClientRegistrar(private val registry: BeanDefinitionRegistry) {
    companion object {
        private val log = LoggerFactory.getLogger(ApiClientRegistrar::class.java)
    }

    fun register(apiClientDefinitions: Set<ApiClientDefinition>) {
        apiClientDefinitions.forEach {
            register(it)
        }
    }

    fun register(apiClientDefinition: ApiClientDefinition) {
        registerWebClient(registry, apiClientDefinition)
        registerApiClient(registry, apiClientDefinition)
    }

    private fun registerWebClient(registry: BeanDefinitionRegistry, apiClientDefinition: ApiClientDefinition) {
        if (log.isInfoEnabled) {
            log.info("Register WebClient [{}].", apiClientDefinition.webClientBeanName)
        }
        if (registry.containsBeanDefinition(apiClientDefinition.webClientBeanName)) {
            if (log.isWarnEnabled) {
                log.warn(
                    "WebClient [{}] already exists - Ignore.",
                    apiClientDefinition.webClientBeanName
                )
            }
            return
        }
        val beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebClientFactoryBean::class.java)
        beanDefinitionBuilder.addConstructorArgValue(apiClientDefinition)
        registry.registerBeanDefinition(apiClientDefinition.webClientBeanName, beanDefinitionBuilder.beanDefinition)
    }

    private fun registerApiClient(registry: BeanDefinitionRegistry, apiClientDefinition: ApiClientDefinition) {
        if (log.isInfoEnabled) {
            log.info("Register ApiClient [{}].", apiClientDefinition.apiClientBeanName)
        }
        if (registry.containsBeanDefinition(apiClientDefinition.apiClientBeanName)) {
            if (log.isWarnEnabled) {
                log.warn(
                    "ApiClient [{}] already exists - Ignore.",
                    apiClientDefinition.apiClientBeanName
                )
            }
            return
        }
        val beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ApiClientFactoryBean::class.java)
        beanDefinitionBuilder.addConstructorArgValue(apiClientDefinition)
        registry.registerBeanDefinition(apiClientDefinition.apiClientBeanName, beanDefinitionBuilder.beanDefinition)
    }
}