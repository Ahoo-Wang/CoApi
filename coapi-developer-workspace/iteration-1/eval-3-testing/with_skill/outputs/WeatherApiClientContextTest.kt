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

package me.ahoo.coapi.example.weather.client

import me.ahoo.coapi.spring.ClientMode
import me.ahoo.coapi.spring.EnableCoApi
import me.ahoo.coapi.spring.client.reactive.ReactiveHttpExchangeAdapterFactory
import me.ahoo.coapi.spring.client.reactive.WebClientBuilderCustomizer
import me.ahoo.coapi.spring.client.sync.RestClientBuilderCustomizer
import me.ahoo.coapi.spring.client.sync.SyncHttpExchangeAdapterFactory
import me.ahoo.test.asserts.assert
import org.assertj.core.api.AssertionsForInterfaceTypes
import org.junit.jupiter.api.Test
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration
import org.springframework.web.client.RestClient

class WeatherApiClientContextTest {

    @Test
    fun `should register Reactive WeatherApiClient bean`() {
        ApplicationContextRunner()
            .withPropertyValues("weather.url=https://api.weather.example.com")
            .withBean(WebClientBuilderCustomizer::class.java, { WebClientBuilderCustomizer.NoOp })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(EnableWeatherClientConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasSingleBean(ReactiveHttpExchangeAdapterFactory::class.java)
                    .hasSingleBean(WeatherApiClient::class.java)

                val weatherClient = context.getBean(WeatherApiClient::class.java)
                weatherClient.assert().isNotNull()
            }
    }

    @Test
    fun `should register Sync WeatherApiClient bean`() {
        ApplicationContextRunner()
            .withPropertyValues("${ClientMode.COAPI_CLIENT_MODE_PROPERTY}=SYNC")
            .withPropertyValues("weather.url=https://api.weather.example.com")
            .withBean(RestClientBuilderCustomizer::class.java, { RestClientBuilderCustomizer.NoOp })
            .withBean(RestClient.Builder::class.java, { RestClient.builder() })
            .withUserConfiguration(RestClientAutoConfiguration::class.java)
            .withUserConfiguration(EnableWeatherClientConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasSingleBean(SyncHttpExchangeAdapterFactory::class.java)
                    .hasSingleBean(WeatherApiClient::class.java)

                val weatherClient = context.getBean(WeatherApiClient::class.java)
                weatherClient.assert().isNotNull()
            }
    }
}

@EnableCoApi(clients = [WeatherApiClient::class])
class EnableWeatherClientConfiguration
