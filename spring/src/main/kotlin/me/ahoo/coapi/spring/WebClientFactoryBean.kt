package me.ahoo.coapi.spring

import org.springframework.beans.factory.FactoryBean
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.web.reactive.function.client.WebClient

/**
 * @see org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction
 */
class WebClientFactoryBean(private val definition: ApiClientDefinition) :
    FactoryBean<WebClient>,
    ApplicationContextAware {

    private lateinit var applicationContext: ApplicationContext
    override fun getObject(): WebClient {
        val clientBuilder = applicationContext.getBean(WebClient.Builder::class.java)
        clientBuilder.baseUrl(definition.baseUrl)
        if (definition.loadBalanced) {
            val loadBalancedExchangeFilterFunction =
                applicationContext.getBean(LoadBalancedExchangeFilterFunction::class.java)
            clientBuilder.filter(loadBalancedExchangeFilterFunction)
        }
        return clientBuilder.build()
    }

    override fun getObjectType(): Class<*> {
        return WebClient::class.java
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
}
