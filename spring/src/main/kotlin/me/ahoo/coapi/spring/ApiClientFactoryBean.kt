package me.ahoo.coapi.spring

import org.springframework.beans.factory.FactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

class ApiClientFactoryBean(
    private val apiClientDefinition: ApiClientDefinition
) : FactoryBean<Any>, ApplicationContextAware {

    private lateinit var applicationContext: ApplicationContext
    override fun getObject(): Any {
        val webClient = applicationContext.getBean(apiClientDefinition.webClientBeanName, WebClient::class.java)
        val clientAdapter = WebClientAdapter.create(webClient)
        val httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(clientAdapter).build()
        return httpServiceProxyFactory.createClient(apiClientDefinition.clientType)
    }

    override fun getObjectType(): Class<*> {
        return apiClientDefinition.clientType
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
}
