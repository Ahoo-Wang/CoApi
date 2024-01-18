package me.ahoo.coapi.spring

import org.springframework.beans.factory.FactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

abstract class AbstractWebClientFactoryBean(private val definition: CoApiDefinition) :
    FactoryBean<WebClient>,
    ApplicationContextAware {
    protected lateinit var appContext: ApplicationContext

    protected open val filterCustomizer: (MutableList<ExchangeFilterFunction>) -> Unit = {}

    override fun getObjectType(): Class<*> {
        return WebClient::class.java
    }

    override fun getObject(): WebClient {
        val clientBuilder = appContext.getBean(WebClient.Builder::class.java)
        clientBuilder.baseUrl(definition.baseUrl)
        val filters = getFilters()
        filters.distinct().forEach {
            clientBuilder.filter(it)
        }
        return clientBuilder.build()
    }

    protected fun getFilters(): List<ExchangeFilterFunction> {
        return buildList {
            definition.filters.forEach { filterName ->
                val filter = appContext.getBean(filterName, ExchangeFilterFunction::class.java)
                add(filter)
            }
            definition.filterTypes.forEach { filterType ->
                val filter = appContext.getBean(filterType)
                add(filter)
            }
            filterCustomizer(this)
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.appContext = applicationContext
    }
}
