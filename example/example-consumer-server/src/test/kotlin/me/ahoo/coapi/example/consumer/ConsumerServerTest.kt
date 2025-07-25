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
import me.ahoo.coapi.example.consumer.client.ServiceApiClient
import me.ahoo.coapi.spring.client.reactive.ReactiveHttpExchangeAdapterFactory
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ConsumerServerTest {
    @Autowired
    private lateinit var httpExchangeAdapterFactory: ReactiveHttpExchangeAdapterFactory

    @Autowired
    private lateinit var gitHubApiClient: GitHubApiClient

    @Autowired
    private lateinit var serviceApiClient: ServiceApiClient

    @Test
    fun httpExchangeAdapterFactoryIsNotNull() {
        MatcherAssert.assertThat(httpExchangeAdapterFactory, Matchers.notNullValue())
    }

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
