package com.example.userservice.controller

import com.example.userservice.client.UserApiClient
import com.example.userservice.model.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class UserController(private val userApiClient: UserApiClient) {

    @GetMapping("/consumer/users/{id}")
    fun getUser(@PathVariable id: String): Mono<User> {
        return userApiClient.getUser(id)
    }

    @GetMapping("/consumer/users")
    fun listUsers(): Flux<User> {
        return userApiClient.listUsers()
    }
}
