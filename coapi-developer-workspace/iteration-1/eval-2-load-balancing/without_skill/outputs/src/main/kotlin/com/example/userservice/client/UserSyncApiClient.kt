package com.example.userservice.client

import com.example.userservice.model.User
import me.ahoo.coapi.api.CoApi
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange

/**
 * Synchronous CoApi client for user-service.
 *
 * Uses @CoApi with serviceId = "user-service" which automatically resolves to
 * "lb://user-service" -- a load-balanced URL. CoApi converts the lb:// prefix
 * to http://user-service and registers a LoadBalancerInterceptor on the
 * RestClient so that Spring Cloud LoadBalancer resolves the service ID to
 * actual host:port addresses discovered via Consul.
 *
 * Return types are plain Kotlin types (blocking) suitable for servlet-based
 * applications. The coapi.mode property must be set to SYNC (or the
 * application must not include WebFlux on the classpath so that AUTO detects
 * sync mode).
 */
@CoApi(serviceId = "user-service")
interface UserSyncApiClient {

    @GetExchange("/users/{id}")
    fun getUser(@PathVariable id: String): User

    @GetExchange("/users")
    fun listUsers(): List<User>
}
