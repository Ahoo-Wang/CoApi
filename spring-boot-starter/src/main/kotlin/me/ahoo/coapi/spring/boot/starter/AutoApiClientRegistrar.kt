package me.ahoo.coapi.spring.boot.starter

import me.ahoo.coapi.api.ApiClient
import me.ahoo.coapi.spring.AbstractApiClientRegistrar
import me.ahoo.coapi.spring.ApiClientDefinition
import me.ahoo.coapi.spring.ApiClientDefinition.Companion.toApiClientDefinition
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter

class AutoApiClientRegistrar :
    AbstractApiClientRegistrar(),
    BeanFactoryAware {
    private lateinit var beanFactory: BeanFactory

    override fun getApiClientDefinitions(importingClassMetadata: AnnotationMetadata): Set<ApiClientDefinition> {
        val scanBasePackages = AutoConfigurationPackages.get(beanFactory).toSet()
        return scanBasePackages.toApiClientDefinitions()
    }

    private fun Set<String>.toApiClientDefinitions(): Set<ApiClientDefinition> {
        val scanner = ApiClientScanner()
        return flatMap { basePackage ->
            scanner.findCandidateComponents(basePackage)
        }.map { beanDefinition ->
            Class.forName(beanDefinition.beanClassName).toApiClientDefinition(env)
        }.toSet()
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }
}

class ApiClientScanner : ClassPathScanningCandidateComponentProvider(false) {
    init {
        addIncludeFilter(AnnotationTypeFilter(ApiClient::class.java))
    }

    override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {
        return beanDefinition.metadata.isIndependent && !beanDefinition.metadata.isAnnotation
    }
}
