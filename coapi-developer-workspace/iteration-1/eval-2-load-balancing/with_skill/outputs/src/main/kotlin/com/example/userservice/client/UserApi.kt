package com.example.userservice.client

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Shared API interface for user-service endpoints.
 *
 * This interface uses standard Spring @HttpExchange annotations.
 * Return types determine the programming model:
 * - Mono<T> / Flux<T> for reactive
 * - T / List<T> for sync
 *
 * The @CoApi annotation on the client interfaces (ReactiveUserClient, SyncUserClient)
 * will use this as the base contract, targeting "user-service" via Consul discovery.
 */
@HttpExchange("users")
interface UserApi {

    @GetExchange("/{id}")
    fun getUser(@PathVariable id: String): Mono<User>

    @GetExchange
    fun listUsers(): Flux<User>
}

data class User(
    val id: String,
    val name: String,
    val email: String
)
