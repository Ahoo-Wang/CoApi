package me.ahoo.coapi.example.sync

import me.ahoo.coapi.spring.client.sync.SyncHttpExchangeAdapterFactory
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ExampleServerTest {
    @Autowired
    private lateinit var httpExchangeAdapterFactory: SyncHttpExchangeAdapterFactory

    @Autowired
    private lateinit var gitHubApiClient: GitHubSyncClient

    @Autowired
    private lateinit var serviceApiClient: GitHubSyncLbClient

    @Test
    fun httpExchangeAdapterFactoryIsNotNull() {
        assertThat(httpExchangeAdapterFactory, notNullValue())
    }

    @Test
    fun getIssueByGitHubApiClient() {
        gitHubApiClient.getIssue("Ahoo-Wang", "Wow")
    }

    @Test
    fun getIssueByServiceApiClient() {
        serviceApiClient.getIssue("Ahoo-Wang", "Wow")
    }
}
