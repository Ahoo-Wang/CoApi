package com.example.userservice.client

import com.example.userservice.model.User
import me.ahoo.coapi.api.CoApi
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Reactive CoApi client for user-service.
 *
 * Uses @CoApi with serviceId = "user-service" which automatically resolves to
 * "lb://user-service" -- a load-balanced URL. CoApi converts the lb:// prefix
 * to http://user-service and registers a LoadBalancedExchangeFilterFunction on
 * the WebClient so that Spring Cloud LoadBalancer resolves the service ID to
 * actual host:port addresses discovered via Consul.
 *
 * Return types are Flux/Mono for fully non-blocking reactive calls.
 */
@CoApi(serviceId = "user-service")
interface UserApiClient {

    @GetExchange("/users/{id}")
    fun getUser(@PathVariable id: String): Mono<User>

    @GetExchange("/users")
    fun listUsers(): Flux<User>
}
