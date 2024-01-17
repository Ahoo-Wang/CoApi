package me.ahoo.coapi.spring.boot.starter

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import

@AutoConfiguration
@ConditionalOnApiClientEnabled
@Import(AutoApiClientRegistrar::class)
@EnableConfigurationProperties(ApiClientProperties::class)
class ApiClientAutoConfiguration
