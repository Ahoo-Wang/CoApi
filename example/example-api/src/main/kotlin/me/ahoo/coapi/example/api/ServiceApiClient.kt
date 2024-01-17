package me.ahoo.coapi.example.api

import me.ahoo.coapi.api.CoApi
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import reactor.core.publisher.Flux

@CoApi(serviceId = "github-service", name = "GitHubApi")
interface ServiceApiClient {

    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssue(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}
