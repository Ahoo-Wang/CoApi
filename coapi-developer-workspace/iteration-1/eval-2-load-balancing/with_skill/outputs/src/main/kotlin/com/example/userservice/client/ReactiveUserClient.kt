package com.example.userservice.client

import me.ahoo.coapi.api.CoApi

/**
 * Reactive (WebClient-based) load-balanced client for user-service.
 *
 * Uses @CoApi with serviceId to resolve via Spring Cloud LoadBalancer + Consul.
 * The serviceId "user-service" will be resolved to lb://user-service automatically,
 * which means Spring Cloud LoadBalancer will look up the service instances from
 * Consul and distribute requests across them.
 *
 * Return types (Mono<T>, Flux<T>) indicate this is a reactive client.
 * CoApi will auto-detect and use WebClient as the underlying HTTP client.
 */
@CoApi(serviceId = "user-service")
interface ReactiveUserClient : UserApi
