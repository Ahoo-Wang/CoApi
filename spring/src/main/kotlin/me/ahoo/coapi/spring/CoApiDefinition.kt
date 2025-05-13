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

data class CoApiDefinition(
    val name: String,
    val apiType: Class<*>,
    val baseUrl: String,
    val loadBalanced: Boolean,
) {
    companion object {
        private const val CLIENT_BEAN_NAME_SUFFIX = ".HttpClient"
        private const val COAPI_BEAN_NAME_SUFFIX = ".CoApi"
        private const val LB_PROTOCOL_PREFIX = "lb://"
        private const val HTTP_PROTOCOL_PREFIX = "http://"

        fun Class<*>.toCoApiDefinition(environment: Environment): CoApiDefinition {
            val coApi = getAnnotation(CoApi::class.java)
                ?: throw IllegalArgumentException("The class must be annotated by @CoApi.")
            var loadBalanced = false
            var baseUrl = ""
            if (coApi.baseUrl.isNotBlank()) {
                baseUrl = environment.resolvePlaceholders(coApi.baseUrl)
                if (baseUrl.startsWith(LB_PROTOCOL_PREFIX)) {
                    loadBalanced = true
                    baseUrl = HTTP_PROTOCOL_PREFIX + baseUrl.substring(LB_PROTOCOL_PREFIX.length)
                }
            } else if (coApi.serviceId.isNotBlank()) {
                loadBalanced = true
                baseUrl = HTTP_PROTOCOL_PREFIX + environment.resolvePlaceholders(coApi.serviceId)
            }

            return CoApiDefinition(
                name = resolveClientName(coApi),
                apiType = this,
                baseUrl = baseUrl,
                loadBalanced = loadBalanced
            )
        }

        private fun Class<*>.resolveClientName(coApi: CoApi): String {
            if (coApi.name.isNotBlank()) {
                return coApi.name
            }
            return simpleName
        }
    }

    val httpClientBeanName: String by lazy {
        name + CLIENT_BEAN_NAME_SUFFIX
    }

    val coApiBeanName: String by lazy {
        name + COAPI_BEAN_NAME_SUFFIX
    }
}
