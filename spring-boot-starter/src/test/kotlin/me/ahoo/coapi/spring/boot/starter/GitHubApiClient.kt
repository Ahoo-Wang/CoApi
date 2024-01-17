package me.ahoo.coapi.spring.boot.starter

import me.ahoo.coapi.api.CoApi
import me.ahoo.coapi.example.api.Issue
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import reactor.core.publisher.Flux

@CoApi(baseUrl = "\${github.url}")
interface GitHubApiClient {

    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssue(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}
