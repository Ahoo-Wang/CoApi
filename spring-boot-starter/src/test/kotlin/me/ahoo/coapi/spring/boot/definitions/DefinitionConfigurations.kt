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

package me.ahoo.coapi.spring.boot.definitions

import me.ahoo.coapi.example.consumer.client.ServiceApiClient
import me.ahoo.coapi.spring.CoApiDefinition
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

// Kept outside the starter test package so other tests' component scanning does not pick these up.

@Configuration(proxyBeanMethods = true)
class StaticDefinitionConfiguration {
    @Value("\${static.url}")
    lateinit var url: String

    companion object {
        @JvmStatic
        @Bean
        fun staticDefinition(environment: Environment): CoApiDefinition {
            return CoApiDefinition(
                name = "StaticDefinition",
                apiType = ServiceApiClient::class.java,
                baseUrl = environment.resolveRequiredPlaceholders("\${static.url}"),
                loadBalanced = false
            )
        }
    }
}

@Configuration(proxyBeanMethods = false)
class InstanceDefinitionConfiguration {
    @Bean
    fun instanceDefinition(): CoApiDefinition {
        return CoApiDefinition("InstanceDefinition", ServiceApiClient::class.java, "http://instance", false)
    }
}
