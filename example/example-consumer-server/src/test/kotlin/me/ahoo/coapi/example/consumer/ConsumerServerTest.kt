/*
 * Copyright [2022-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.coapi.example.consumer

import com.sun.net.httpserver.HttpServer
import me.ahoo.coapi.example.consumer.client.GitHubApiClient
import me.ahoo.coapi.example.consumer.client.Issue
import me.ahoo.coapi.example.consumer.client.ServiceApiClient
import me.ahoo.coapi.example.consumer.client.ServiceApiClientUseFilterBeanName
import me.ahoo.coapi.example.consumer.client.ServiceApiClientUseFilterType
import me.ahoo.coapi.spring.client.reactive.ReactiveHttpExchangeAdapterFactory
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import reactor.core.publisher.Flux
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Runs against a local stand-in for the GitHub API instead of api.github.com, whose rate limiting
 * made CI flaky (403). Both entry points are redirected: `github.url` (direct base URL) and the
 * `github-service` instance used by the load-balanced clients.
 */
@SpringBootTest
class ConsumerServerTest {
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
    private lateinit var httpExchangeAdapterFactory: ReactiveHttpExchangeAdapterFactory

    @Autowired
    private lateinit var gitHubApiClient: GitHubApiClient

    @Autowired
    private lateinit var serviceApiClient: ServiceApiClient

    @Autowired
    private lateinit var serviceApiClientUseFilterBeanName: ServiceApiClientUseFilterBeanName

    @Autowired
    private lateinit var serviceApiClientUseFilterType: ServiceApiClientUseFilterType

    private fun Flux<Issue>.assertIssues() {
        val issues = collectList().block()
        MatcherAssert.assertThat(issues, Matchers.contains(Issue("https://api.github.com$ISSUES_PATH/1")))
        MatcherAssert.assertThat(requestedPaths, Matchers.hasItem(ISSUES_PATH))
    }

    @Test
    fun httpExchangeAdapterFactoryIsNotNull() {
        MatcherAssert.assertThat(httpExchangeAdapterFactory, Matchers.notNullValue())
    }

    @Test
    fun getIssueByGitHubApiClient() {
        gitHubApiClient.getIssue("Ahoo-Wang", "Wow").assertIssues()
    }

    @Test
    fun getIssueByServiceApiClient() {
        serviceApiClient.getIssue("Ahoo-Wang", "Wow").assertIssues()
    }

    @Test
    fun getIssueByServiceApiClientUseFilterBeanName() {
        serviceApiClientUseFilterBeanName.getIssue("Ahoo-Wang", "Wow").assertIssues()
    }

    @Test
    fun getIssueByServiceApiClientUseFilterType() {
        serviceApiClientUseFilterType.getIssue("Ahoo-Wang", "Wow").assertIssues()
    }
}
