package me.ahoo.coapi.example

import me.ahoo.coapi.spring.EnableApiClients
import me.ahoo.coapi.example.client.GitHubApiClient
import me.ahoo.coapi.example.client.ServiceApiClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableApiClients(clients = [GitHubApiClient::class, ServiceApiClient::class])
@SpringBootApplication
class ExampleServer

fun main(args: Array<String>) {
    runApplication<ExampleServer>(*args)
}
