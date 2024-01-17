package me.ahoo.coapi.spring

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanNameGenerator
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotationMetadata

abstract class AbstractApiClientRegistrar : ImportBeanDefinitionRegistrar, EnvironmentAware {
    companion object {
        private val log = LoggerFactory.getLogger(AbstractApiClientRegistrar::class.java)
    }

    protected lateinit var env: Environment

    private val registrarName = this.javaClass.simpleName

    override fun registerBeanDefinitions(
        importingClassMetadata: AnnotationMetadata,
        registry: BeanDefinitionRegistry,
        importBeanNameGenerator: BeanNameGenerator
    ) {
        getApiClientDefinitions(importingClassMetadata).forEach { apiClientDefinition ->
            registerApi(registry, apiClientDefinition)
        }
    }

    abstract fun getApiClientDefinitions(importingClassMetadata: AnnotationMetadata): Set<ApiClientDefinition>

    protected fun registerApi(registry: BeanDefinitionRegistry, apiClientDefinition: ApiClientDefinition) {
        registerWebClient(registry, apiClientDefinition)
        registerApiClient(registry, apiClientDefinition)
    }

    protected fun registerWebClient(registry: BeanDefinitionRegistry, apiClientDefinition: ApiClientDefinition) {
        if (log.isInfoEnabled) {
            log.info("$registrarName - Register WebClient [{}].", apiClientDefinition.webClientBeanName)
        }
        if (registry.containsBeanDefinition(apiClientDefinition.webClientBeanName)) {
            if (log.isWarnEnabled) {
                log.warn(
                    "$registrarName - WebClient [{}] already exists - Ignore.",
                    apiClientDefinition.webClientBeanName
                )
            }
            return
        }
        val beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebClientFactoryBean::class.java)
        beanDefinitionBuilder.addConstructorArgValue(apiClientDefinition)
        registry.registerBeanDefinition(apiClientDefinition.webClientBeanName, beanDefinitionBuilder.beanDefinition)
    }

    protected fun registerApiClient(registry: BeanDefinitionRegistry, apiClientDefinition: ApiClientDefinition) {
        if (log.isInfoEnabled) {
            log.info("$registrarName - Register ApiClient [{}].", apiClientDefinition.apiClientBeanName)
        }
        if (registry.containsBeanDefinition(apiClientDefinition.apiClientBeanName)) {
            if (log.isWarnEnabled) {
                log.warn(
                    "$registrarName - ApiClient [{}] already exists - Ignore.",
                    apiClientDefinition.apiClientBeanName
                )
            }
            return
        }
        val beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ApiClientFactoryBean::class.java)
        beanDefinitionBuilder.addConstructorArgValue(apiClientDefinition)
        registry.registerBeanDefinition(apiClientDefinition.apiClientBeanName, beanDefinitionBuilder.beanDefinition)
    }

    override fun setEnvironment(environment: Environment) {
        this.env = environment
    }
}
