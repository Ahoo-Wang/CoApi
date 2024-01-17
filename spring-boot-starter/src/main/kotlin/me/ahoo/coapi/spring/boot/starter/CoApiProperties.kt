package me.ahoo.coapi.spring.boot.starter

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

const val COAPI_PREFIX = "coapi"
const val ENABLED_SUFFIX_KEY = ".enabled"

@ConfigurationProperties(prefix = COAPI_PREFIX)
data class CoApiProperties(
    @DefaultValue("true") var enabled: Boolean = true
)
