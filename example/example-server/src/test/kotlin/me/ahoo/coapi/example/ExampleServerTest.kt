package me.ahoo.coapi.example

import me.ahoo.coapi.example.api.GitHubApiClient
import me.ahoo.coapi.example.api.ServiceApiClient
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ExampleServerTest {
    @Autowired
    private lateinit var gitHubApiClient: GitHubApiClient

    @Autowired
    private lateinit var serviceApiClient: ServiceApiClient

    @Test
    fun getIssueByGitHubApiClient() {
        gitHubApiClient.getIssue("Ahoo-Wang", "Wow")
            .doOnNext { println(it) }
            .blockLast()
    }

    @Test
    fun getIssueByServiceApiClient() {
        serviceApiClient.getIssue("Ahoo-Wang", "Wow")
            .doOnNext { println(it) }
            .blockLast()
    }
}