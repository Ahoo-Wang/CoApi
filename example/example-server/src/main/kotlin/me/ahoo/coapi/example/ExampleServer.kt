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

package me.ahoo.coapi.example

import me.ahoo.coapi.example.api.GitHubApiClient
import me.ahoo.coapi.example.api.ServiceApiClient
import me.ahoo.coapi.example.api.ServiceApiClientUseFilterBeanName
import me.ahoo.coapi.example.api.ServiceApiClientUseFilterType
import me.ahoo.coapi.spring.EnableCoApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableCoApi(
    apis = [
        GitHubApiClient::class,
        ServiceApiClient::class,
        ServiceApiClientUseFilterBeanName::class,
        ServiceApiClientUseFilterType::class
    ]
)
@SpringBootApplication
class ExampleServer

fun main(args: Array<String>) {
    runApplication<ExampleServer>(*args)
}
