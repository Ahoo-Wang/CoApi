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

import me.ahoo.coapi.api.CoApi
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import reactor.core.publisher.Mono

@CoApi(baseUrl = "\${weather.api.base-url}", name = "WeatherApi")
interface WeatherApiClient {

    @GetExchange("v1/current")
    fun getCurrentWeather(
        @RequestParam city: String,
        @RequestParam(defaultValue = "metric") units: String = "metric"
    ): Mono<WeatherResponse>

    @GetExchange("v1/forecast/{city}")
    fun getForecast(@PathVariable city: String): Mono<ForecastResponse>
}

data class WeatherResponse(
    val city: String,
    val temperature: Double,
    val humidity: Int,
    val description: String
)

data class ForecastResponse(
    val city: String,
    val forecasts: List<DailyForecast>
)

data class DailyForecast(
    val date: String,
    val temperatureHigh: Double,
    val temperatureLow: Double,
    val description: String
)
