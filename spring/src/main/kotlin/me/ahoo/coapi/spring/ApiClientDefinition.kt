package me.ahoo.coapi.spring

import me.ahoo.coapi.api.ApiClient
import org.springframework.core.env.Environment

data class ApiClientDefinition(
    val name: String,
    val clientType: Class<*>,
    val baseUrl: String,
    val loadBalanced: Boolean,
) {
    companion object {
        private const val WEB_CLIENT_BEAN_NAME_SUFFIX = ".WebClient"
        private const val API_CLIENT_BEAN_NAME_SUFFIX = ".ApiClient"
        private const val LB_SCHEME_PREFIX = "http://"

        fun Class<*>.toApiClientDefinition(environment: Environment): ApiClientDefinition {
            val apiClient = getAnnotation(ApiClient::class.java)
                ?: throw IllegalArgumentException("The class must be annotated by @ApiClient.")
            require(apiClient.serviceId.isNotBlank() || apiClient.baseUrl.isNotBlank()) {
                "The @ApiClient must be configured by serviceId or baseUrl."
            }
            val baseUrl = apiClient.resolveBaseUrl(environment)
            return ApiClientDefinition(
                name = resolveClientName(apiClient),
                clientType = this,
                baseUrl = baseUrl,
                loadBalanced = apiClient.serviceId.isNotBlank(),
            )
        }

        private fun ApiClient.resolveBaseUrl(environment: Environment): String {
            if (serviceId.isNotBlank()) {
                return LB_SCHEME_PREFIX + serviceId
            }
            return environment.resolvePlaceholders(baseUrl)
        }

        private fun Class<*>.resolveClientName(apiClient: ApiClient): String {
            if (apiClient.name.isNotBlank()) {
                return apiClient.name
            }
            return simpleName
        }
    }

    val webClientBeanName: String by lazy {
        name + WEB_CLIENT_BEAN_NAME_SUFFIX
    }

    val apiClientBeanName: String by lazy {
        name + API_CLIENT_BEAN_NAME_SUFFIX
    }
}
