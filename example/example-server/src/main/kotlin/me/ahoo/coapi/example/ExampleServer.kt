package me.ahoo.coapi.example

import me.ahoo.coapi.example.api.GitHubApiClient
import me.ahoo.coapi.example.api.ServiceApiClient
import me.ahoo.coapi.spring.EnableCoApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableCoApi(apis = [GitHubApiClient::class, ServiceApiClient::class])
@SpringBootApplication
class ExampleServer

fun main(args: Array<String>) {
    runApplication<ExampleServer>(*args)
}
