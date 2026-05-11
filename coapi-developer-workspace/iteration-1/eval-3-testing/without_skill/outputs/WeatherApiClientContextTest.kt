/*
 * Copyright [2022-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.coapi.weather.client

import me.ahoo.coapi.spring.ClientMode
import me.ahoo.coapi.spring.EnableCoApi
import me.ahoo.coapi.spring.client.ClientProperties
import me.ahoo.coapi.spring.client.reactive.ReactiveHttpExchangeAdapterFactory
import me.ahoo.coapi.spring.client.reactive.WebClientBuilderCustomizer
import me.ahoo.coapi.spring.client.sync.RestClientBuilderCustomizer
import me.ahoo.coapi.spring.client.sync.SyncHttpExchangeAdapterFactory
import me.ahoo.test.asserts.assert
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration
import org.springframework.web.client.RestClient

class WeatherApiClientContextTest {

    @Test
    fun `should register WeatherApiClient bean in reactive mode`() {
        ApplicationContextRunner()
            .withPropertyValues("weather.api.base-url=https://api.weather.example.com")
            .withBean(WebClientBuilderCustomizer::class.java, { WebClientBuilderCustomizer.NoOp })
            .withBean(ClientProperties::class.java, { MockWeatherClientProperties() })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(WeatherApiEnableCoApiConfiguration::class.java)
            .run { context ->
                context.hasSingleBean(ReactiveHttpExchangeAdapterFactory::class.java).assert().isTrue()
                context.hasSingleBean(WeatherApiClient::class.java).assert().isTrue()

                val weatherApiClient = context.getBean(WeatherApiClient::class.java)
                weatherApiClient.assert().isNotNull()
            }
    }

    @Test
    fun `should register WeatherApiClient bean in sync mode`() {
        ApplicationContextRunner()
            .withPropertyValues("weather.api.base-url=https://api.weather.example.com")
            .withPropertyValues("${ClientMode.COAPI_CLIENT_MODE_PROPERTY}=SYNC")
            .withBean(RestClientBuilderCustomizer::class.java, { RestClientBuilderCustomizer.NoOp })
            .withBean(RestClient.Builder::class.java, { RestClient.builder() })
            .withBean(ClientProperties::class.java, { MockWeatherClientProperties() })
            .withUserConfiguration(WeatherApiEnableCoApiConfiguration::class.java)
            .run { context ->
                context.hasSingleBean(SyncHttpExchangeAdapterFactory::class.java).assert().isTrue()
                context.hasSingleBean(WeatherApiClient::class.java).assert().isTrue()

                val weatherApiClient = context.getBean(WeatherApiClient::class.java)
                weatherApiClient.assert().isNotNull()
            }
    }

    @Test
    fun `should fail when base-url property is missing`() {
        ApplicationContextRunner()
            .withBean(WebClientBuilderCustomizer::class.java, { WebClientBuilderCustomizer.NoOp })
            .withBean(ClientProperties::class.java, { MockWeatherClientProperties() })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(WeatherApiEnableCoApiConfiguration::class.java)
            .run { context ->
                // The context should fail because the placeholder ${weather.api.base-url}
                // cannot be resolved
                context.startupFailure.assert().isNotNull()
            }
    }

    @Test
    fun `should register bean with expected name derived from CoApi name attribute`() {
        ApplicationContextRunner()
            .withPropertyValues("weather.api.base-url=https://api.weather.example.com")
            .withBean(WebClientBuilderCustomizer::class.java, { WebClientBuilderCustomizer.NoOp })
            .withBean(ClientProperties::class.java, { MockWeatherClientProperties() })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(WeatherApiEnableCoApiConfiguration::class.java)
            .run { context ->
                // The bean name follows the convention: {coApiName}.CoApi
                context.hasBean("WeatherApi.CoApi").assert().isTrue()
                // The HttpClient bean name follows: {coApiName}.HttpClient
                context.hasBean("WeatherApi.HttpClient").assert().isTrue()
            }
    }
}

@EnableCoApi(clients = [WeatherApiClient::class])
class WeatherApiEnableCoApiConfiguration

data class MockWeatherClientProperties(
    val filter: Map<String, ClientProperties.FilterDefinition> = emptyMap(),
    val interceptor: Map<String, ClientProperties.InterceptorDefinition> = emptyMap(),
) : ClientProperties {
    override fun getBaseUri(coApiName: String): String {
        return ""
    }

    override fun getLoadBalanced(coApiName: String): Boolean? {
        return null
    }

    override fun getFilter(coApiName: String): ClientProperties.FilterDefinition {
        return filter[coApiName] ?: ClientProperties.FilterDefinition()
    }

    override fun getInterceptor(coApiName: String): ClientProperties.InterceptorDefinition {
        return interceptor[coApiName] ?: ClientProperties.InterceptorDefinition()
    }
}
