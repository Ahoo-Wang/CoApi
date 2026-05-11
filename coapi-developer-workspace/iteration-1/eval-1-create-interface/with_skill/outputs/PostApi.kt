package me.ahoo.coapi.example.jsonplaceholder.api

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@HttpExchange("posts")
interface PostApi {

    @GetExchange
    fun getPosts(): Flux<Post>

    @GetExchange("{id}")
    fun getPostById(@PathVariable id: Int): Mono<Post>

    @PostExchange
    fun createPost(@RequestBody post: CreatePostRequest): Mono<Post>
}

data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

data class CreatePostRequest(
    val userId: Int,
    val title: String,
    val body: String
)
