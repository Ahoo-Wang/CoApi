package me.ahoo.coapi.spring

import org.springframework.beans.factory.FactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

class CoApiFactoryBean(
    private val coApiDefinition: CoApiDefinition
) : FactoryBean<Any>, ApplicationContextAware {

    private lateinit var applicationContext: ApplicationContext
    override fun getObject(): Any {
        val webClient = applicationContext.getBean(coApiDefinition.webClientBeanName, WebClient::class.java)
        val clientAdapter = WebClientAdapter.create(webClient)
        val httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(clientAdapter).build()
        return httpServiceProxyFactory.createClient(coApiDefinition.apiType)
    }

    override fun getObjectType(): Class<*> {
        return coApiDefinition.apiType
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
}
