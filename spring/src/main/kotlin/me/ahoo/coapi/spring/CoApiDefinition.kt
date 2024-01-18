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

package me.ahoo.coapi.spring

import me.ahoo.coapi.api.CoApi
import org.springframework.core.env.Environment
import org.springframework.web.reactive.function.client.ExchangeFilterFunction

data class CoApiDefinition(
    val name: String,
    val apiType: Class<*>,
    val baseUrl: String,
    val loadBalanced: Boolean,
    val filters: List<String>,
    val filterTypes: List<Class<out ExchangeFilterFunction>>
) {
    companion object {
        private const val WEB_CLIENT_BEAN_NAME_SUFFIX = ".WebClient"
        private const val CO_API_BEAN_NAME_SUFFIX = ".CoApi"
        private const val LB_SCHEME_PREFIX = "http://"

        fun Class<*>.toCoApiDefinition(environment: Environment): CoApiDefinition {
            val coApi = getAnnotation(CoApi::class.java)
                ?: throw IllegalArgumentException("The class must be annotated by @CoApi.")
            require(coApi.serviceId.isNotBlank() || coApi.baseUrl.isNotBlank()) {
                "The @CoApi must be configured by serviceId or baseUrl."
            }
            val baseUrl = coApi.resolveBaseUrl(environment)
            return CoApiDefinition(
                name = resolveClientName(coApi),
                apiType = this,
                baseUrl = baseUrl,
                loadBalanced = coApi.serviceId.isNotBlank(),
                filters = coApi.filters.distinct().toList(),
                filterTypes = coApi.filterTypes.map { it.java }.distinct().toList()
            )
        }

        private fun CoApi.resolveBaseUrl(environment: Environment): String {
            if (serviceId.isNotBlank()) {
                return LB_SCHEME_PREFIX + serviceId
            }
            return environment.resolvePlaceholders(baseUrl)
        }

        private fun Class<*>.resolveClientName(coApi: CoApi): String {
            if (coApi.name.isNotBlank()) {
                return coApi.name
            }
            return simpleName
        }
    }

    val webClientBeanName: String by lazy {
        name + WEB_CLIENT_BEAN_NAME_SUFFIX
    }

    val coApiBeanName: String by lazy {
        name + CO_API_BEAN_NAME_SUFFIX
    }
}
