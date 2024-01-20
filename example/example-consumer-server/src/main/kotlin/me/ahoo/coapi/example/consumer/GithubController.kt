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

import me.ahoo.coapi.example.consumer.client.GitHubApiClient
import me.ahoo.coapi.example.consumer.client.Issue
import me.ahoo.coapi.example.consumer.client.ServiceApiClient
import me.ahoo.coapi.example.consumer.client.UriApiClient
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.util.DefaultUriBuilderFactory
import reactor.core.publisher.Flux
import java.net.URI

@RestController
@HttpExchange("github")
class GithubController(
    private val gitHubApiClient: GitHubApiClient,
    private val serviceApiClient: ServiceApiClient,
    private val uriApiClient: UriApiClient
) {
    companion object {
        private val log = LoggerFactory.getLogger(GithubController::class.java)
    }

    @GetMapping("/baseUrl")
    fun baseUrl(): Flux<Issue> {
        return gitHubApiClient.getIssue("Ahoo-Wang", "Wow")
    }

    @GetExchange("/serviceId")
    fun serviceId(): Flux<Issue> {
        return serviceApiClient.getIssue("Ahoo-Wang", "CoApi").doOnError(WebClientResponseException::class.java) {
            log.error(it.responseBodyAsString)
        }
    }

    @GetExchange("/uri")
    fun uri(): Flux<Issue> {
        val uri = URI.create("https://api.github.com/repos/Ahoo-Wang/CoApi/issues")
        return uriApiClient.getIssueByUri(uri)
    }

    @GetExchange("/uriBuilder")
    fun uriBuilder(): Flux<Issue> {
        val uriBuilderFactory = DefaultUriBuilderFactory("https://api.github.com/repos/{owner}/{repo}/issues")
        return uriApiClient.getIssue(uriBuilderFactory, "Ahoo-Wang", "Wow")
    }
}
