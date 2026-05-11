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

package me.ahoo.coapi.example.jsonplaceholder.client

import me.ahoo.coapi.api.CoApi
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@CoApi(baseUrl = "\${jsonplaceholder.url}")
interface JsonPlaceholderClient {

    @GetExchange("posts")
    fun getPosts(): Flux<Post>

    @GetExchange("posts/{id}")
    fun getPostById(@PathVariable id: Int): Mono<Post>

    @PostExchange("posts")
    fun createPost(@RequestBody request: CreatePostRequest): Mono<Post>
}
