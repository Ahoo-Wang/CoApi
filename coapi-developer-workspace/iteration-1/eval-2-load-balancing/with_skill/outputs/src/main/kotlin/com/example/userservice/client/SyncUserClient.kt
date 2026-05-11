package com.example.userservice.client

import me.ahoo.coapi.api.CoApi
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange

/**
 * Synchronous (RestClient-based) load-balanced client for user-service.
 *
 * Uses @CoApi with serviceId to resolve via Spring Cloud LoadBalancer + Consul.
 * Return types (User, List<User>) indicate this is a synchronous client.
 * CoApi will use RestClient as the underlying HTTP client when mode=SYNC.
 *
 * Note: This interface re-declares the methods with sync return types
 * instead of extending UserApi (which has reactive return types).
 */
@CoApi(serviceId = "user-service")
interface SyncUserClient {

    @GetExchange("users/{id}")
    fun getUser(@PathVariable id: String): User

    @GetExchange("users")
    fun listUsers(): List<User>
}
