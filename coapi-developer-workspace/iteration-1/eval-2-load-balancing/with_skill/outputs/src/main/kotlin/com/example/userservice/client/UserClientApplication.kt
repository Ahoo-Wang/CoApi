package com.example.userservice.client

import me.ahoo.coapi.spring.EnableCoApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

/**
 * Spring Boot application demonstrating CoApi load-balanced clients.
 *
 * @EnableCoApi explicitly registers the client interfaces.
 * Alternatively, CoApi auto-scans for @CoApi-annotated interfaces
 * in the application's base package, so this annotation is optional
 * if the clients are in the same package tree.
 */
@EnableCoApi(
    clients = [
        ReactiveUserClient::class,
        SyncUserClient::class
    ]
)
@SpringBootApplication
class UserClientApplication

fun main(args: Array<String>) {
    runApplication<UserClientApplication>(*args)
}

/**
 * Example REST controller demonstrating usage of the CoApi clients.
 * In reactive mode, inject ReactiveUserClient.
 * In sync mode, inject SyncUserClient.
 */
@RestController
class UserController(
    private val reactiveUserClient: ReactiveUserClient,
    private val syncUserClient: SyncUserClient
) {
    // Reactive endpoints using WebClient-based client
    @GetMapping("/reactive/users/{id}")
    fun getReactiveUser(@PathVariable id: String) = reactiveUserClient.getUser(id)

    @GetMapping("/reactive/users")
    fun listReactiveUsers() = reactiveUserClient.listUsers()

    // Sync endpoints using RestClient-based client
    @GetMapping("/sync/users/{id}")
    fun getSyncUser(@PathVariable id: String) = syncUserClient.getUser(id)

    @GetMapping("/sync/users")
    fun listSyncUsers() = syncUserClient.listUsers()
}
