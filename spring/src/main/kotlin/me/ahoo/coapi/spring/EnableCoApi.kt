package me.ahoo.coapi.spring

import org.springframework.context.annotation.Import
import kotlin.reflect.KClass

@Import(EnableCoApiRegistrar::class)
@Target(AnnotationTarget.CLASS)
annotation class EnableCoApi(
    val apis: Array<KClass<*>> = []
)
