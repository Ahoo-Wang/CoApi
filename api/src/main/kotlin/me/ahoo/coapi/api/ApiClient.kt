package me.ahoo.coapi.api

import org.springframework.stereotype.Component

@Target(AnnotationTarget.CLASS)
@Component
annotation class ApiClient(
    val serviceId: String = "",
    val baseUrl: String = "",
    val name: String = "",
)
