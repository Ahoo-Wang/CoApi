package me.ahoo.coapi.example

import me.ahoo.coapi.example.api.GitHubApiClient
import me.ahoo.coapi.example.api.Issue
import me.ahoo.coapi.example.api.ServiceApiClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class GithubController(
    private val gitHubApiClient: GitHubApiClient,
    private val serviceApiClient: ServiceApiClient
) {

    @GetMapping("/baseUrl")
    fun baseUrl(): Flux<Issue> {
        return gitHubApiClient.getIssue("Ahoo-Wang", "Wow")
    }

    @GetMapping("/serviceId")
    fun serviceId(): Flux<Issue> {
        return serviceApiClient.getIssue("Ahoo-Wang", "Wow")
    }
}
