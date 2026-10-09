package me.ahoo.coapi.example.sync

import com.sun.net.httpserver.HttpServer
import me.ahoo.coapi.spring.client.sync.SyncHttpExchangeAdapterFactory
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Runs against a local stand-in for the GitHub API instead of api.github.com, whose rate limiting
 * made CI flaky (403). Both entry points are redirected: `github.url` (direct base URL) and the
 * `github-service` instance used by the load-balanced client.
 */
@SpringBootTest
class ExampleServerTest {
    companion object {
        private const val ISSUES_PATH = "/repos/Ahoo-Wang/Wow/issues"
        private const val GITHUB_SERVICE_INSTANCE = "spring.cloud.discovery.client.simple.instances.github-service[0]"
        private val requestedPaths = CopyOnWriteArrayList<String>()

        private val gitHub: HttpServer = HttpServer.create(InetSocketAddress("localhost", 0), 0).apply {
            createContext("/") { exchange ->
                requestedPaths += exchange.requestURI.path
                val body = """[{"url":"https://api.github.com$ISSUES_PATH/1"}]""".toByteArray()
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, body.size.toLong())
                exchange.responseBody.use { it.write(body) }
            }
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun gitHubProperties(registry: DynamicPropertyRegistry) {
            registry.add("github.url") { "http://localhost:${gitHub.address.port}" }
            registry.add("$GITHUB_SERVICE_INSTANCE.host") { "localhost" }
            registry.add(
                "spring.cloud.discovery.client.simple.instances.github-service[0].port"
            ) { gitHub.address.port }
            registry.add("$GITHUB_SERVICE_INSTANCE.secure") { false }
        }

        @JvmStatic
        @AfterAll
        fun stopGitHub() {
            gitHub.stop(0)
        }
    }

    @Autowired
    private lateinit var httpExchangeAdapterFactory: SyncHttpExchangeAdapterFactory

    @Autowired
    private lateinit var gitHubApiClient: GitHubSyncClient

    @Autowired
    private lateinit var serviceApiClient: GitHubSyncLbClient

    private fun List<Issue>?.assertIssues() {
        assertThat(this, contains(Issue("https://api.github.com$ISSUES_PATH/1")))
        assertThat(requestedPaths, hasItem(ISSUES_PATH))
    }

    @Test
    fun httpExchangeAdapterFactoryIsNotNull() {
        assertThat(httpExchangeAdapterFactory, notNullValue())
    }

    @Test
    fun getIssueByGitHubApiClient() {
        gitHubApiClient.getIssue("Ahoo-Wang", "Wow").assertIssues()
    }

    @Test
    fun getIssueByServiceApiClient() {
        serviceApiClient.getIssue("Ahoo-Wang", "Wow").assertIssues()
    }
}
