package me.ahoo.coapi.example.jsonplaceholder.client

import me.ahoo.coapi.api.CoApi
import me.ahoo.coapi.example.jsonplaceholder.api.PostApi

@CoApi(baseUrl = "\${jsonplaceholder.url}")
interface PostClient : PostApi
