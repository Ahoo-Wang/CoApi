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

/**
 * Represents the definition of a CoApi.
 * This class holds information about a CoApi, including its name, type, base URL, and whether it's load balanced.
 */
data class CoApiDefinition(
    /**
     * The name of the CoApi.
     */
    val name: String,
    /**
     * The type of the CoApi.
     */
    val apiType: Class<*>,
    /**
     * The base URL of the CoApi. Supports protocols:
     * - `lb://` for load-balanced services.
     * - `http://` for direct HTTP connections.
     */
    val baseUrl: String,
    /**
     * Indicates whether the CoApi is load balanced.
     */
    val loadBalanced: Boolean,
) {
    companion object {
        /**
         * Suffix for the HttpClient bean name.
         */
        private const val CLIENT_BEAN_NAME_SUFFIX = ".HttpClient"

        /**
         * Suffix for the CoApi bean name.
         */
        private const val COAPI_BEAN_NAME_SUFFIX = ".CoApi"

        /**
         * Prefix for load balanced protocol.
         */
        private const val LB_PROTOCOL_PREFIX = "lb://"

        /**
         * Prefix for HTTP protocol.
         */
        private const val HTTP_PROTOCOL_PREFIX = "http://"

        /**
         * Converts a class to a CoApiDefinition.
         * @param environment The Spring Environment.
         * @return A CoApiDefinition instance.
         */
        fun Class<*>.toCoApiDefinition(environment: Environment): CoApiDefinition {
            // Retrieve the CoApi annotation from the class
            val coApi = getAnnotation(CoApi::class.java)
                ?: throw IllegalArgumentException("The class must be annotated by @CoApi.")

            // Resolve the base URL from the CoApi annotation
            val resolvedBaseUrl = coApi.resolveBaseUrl(environment)

            // Determine if the CoApi is load balanced
            val loadBalanced = resolvedBaseUrl.startsWith(LB_PROTOCOL_PREFIX)

            // Adjust the base URL if it's load balanced
            val baseUrl = if (loadBalanced) {
                HTTP_PROTOCOL_PREFIX + resolvedBaseUrl.substring(LB_PROTOCOL_PREFIX.length)
            } else {
                resolvedBaseUrl
            }

            // Return a new CoApiDefinition instance
            return CoApiDefinition(
                name = resolveClientName(coApi),
                apiType = this,
                baseUrl = baseUrl,
                loadBalanced = loadBalanced
            )
        }

        /**
         * Resolves the base URL from the CoApi annotation.
         * @param environment The Spring Environment.
         * @return The resolved base URL.
         */
        fun CoApi.resolveBaseUrl(environment: Environment): String {
            // If the base URL is not blank, resolve placeholders and return it
            if (baseUrl.isNotBlank()) {
                return environment.resolvePlaceholders(baseUrl)
            }
            // Otherwise, construct a load balanced URL using the service ID
            return LB_PROTOCOL_PREFIX + environment.resolvePlaceholders(serviceId)
        }

        /**
         * Resolves the client name from the CoApi annotation.
         * @param coApi The CoApi annotation.
         * @return The resolved client name.
         */
        private fun Class<*>.resolveClientName(coApi: CoApi): String {
            // If the name in the annotation is not blank, use it
            if (coApi.name.isNotBlank()) {
                return coApi.name
            }
            // Otherwise, use the simple name of the class
            return simpleName
        }
    }

    /**
     * The bean name for the HttpClient associated with this CoApi.
     */
    val httpClientBeanName: String by lazy {
        name + CLIENT_BEAN_NAME_SUFFIX
    }

    /**
     * The bean name for the CoApi itself.
     */
    val coApiBeanName: String by lazy {
        name + COAPI_BEAN_NAME_SUFFIX
    }
}
