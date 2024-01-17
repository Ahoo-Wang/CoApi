package me.ahoo.coapi.spring

import org.springframework.context.annotation.Import
import kotlin.reflect.KClass

@Import(EnableApiClientsRegistrar::class)
@Target(AnnotationTarget.CLASS)
annotation class EnableApiClients(
    val clients: Array<KClass<*>> = []
)
