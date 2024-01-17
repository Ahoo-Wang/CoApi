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
