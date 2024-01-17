package me.ahoo.coapi.spring.boot.starter

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

const val API_CLIENT_PREFIX = "apiclient"
const val ENABLED_SUFFIX_KEY = ".enabled"

@ConfigurationProperties(prefix = API_CLIENT_PREFIX)
data class ApiClientProperties(
    @DefaultValue("true") var enabled: Boolean = true
)
