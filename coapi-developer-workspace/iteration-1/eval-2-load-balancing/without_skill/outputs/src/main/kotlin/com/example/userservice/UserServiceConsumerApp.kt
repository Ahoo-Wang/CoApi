package com.example.userservice

import com.example.userservice.client.UserApiClient
import com.example.userservice.client.UserSyncApiClient
import me.ahoo.coapi.spring.EnableCoApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Application entry point.
 *
 * @EnableCoApi registers the listed client interfaces as Spring beans backed by
 * auto-configured HTTP exchange proxies. CoApi's spring-boot-starter handles
 * the rest: it creates a WebClient (reactive) or RestClient (sync) with
 * load-balancing wired in automatically when serviceId is set.
 */
@EnableCoApi(
    clients = [
        UserApiClient::class,
        UserSyncApiClient::class
    ]
)
@SpringBootApplication
class UserServiceConsumerApp

fun main(args: Array<String>) {
    runApplication<UserServiceConsumerApp>(*args)
}
