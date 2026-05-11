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

import me.ahoo.coapi.spring.EnableCoApi
import me.ahoo.coapi.spring.client.reactive.ReactiveHttpExchangeAdapterFactory
import me.ahoo.test.asserts.assert
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest(classes = [WeatherApiIntegrationTestApp::class])
@TestPropertySource(
    properties = [
        "weather.api.base-url=https://api.weather.example.com"
    ]
)
class WeatherApiClientIntegrationTest {

    @Autowired
    private lateinit var httpExchangeAdapterFactory: ReactiveHttpExchangeAdapterFactory

    @Autowired
    private lateinit var weatherApiClient: WeatherApiClient

    @Test
    fun httpExchangeAdapterFactoryIsNotNull() {
        httpExchangeAdapterFactory.assert().isNotNull()
    }

    @Test
    fun weatherApiClientIsNotNull() {
        weatherApiClient.assert().isNotNull()
    }

    @Test
    fun getCurrentWeather() {
        weatherApiClient.getCurrentWeather(city = "London")
            .doOnNext { response ->
                response.assert().isNotNull()
                response.city.assert().isNotBlank()
                response.description.assert().isNotBlank()
            }
            .block()
    }

    @Test
    fun getCurrentWeatherWithCustomUnits() {
        weatherApiClient.getCurrentWeather(city = "London", units = "imperial")
            .doOnNext { response ->
                response.assert().isNotNull()
                response.city.assert().isEqualTo("London")
            }
            .block()
    }

    @Test
    fun getForecast() {
        weatherApiClient.getForecast(city = "London")
            .doOnNext { response ->
                response.assert().isNotNull()
                response.city.assert().isNotBlank()
                response.forecasts.assert().isNotEmpty()
            }
            .block()
    }
}

@EnableCoApi(clients = [WeatherApiClient::class])
@SpringBootApplication
class WeatherApiIntegrationTestApp
