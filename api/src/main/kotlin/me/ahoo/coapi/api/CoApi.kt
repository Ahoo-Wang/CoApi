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

package me.ahoo.coapi.api

import org.springframework.stereotype.Component

/**
 * CoApi Annotation
 *
 * The `@CoApi` annotation is used to mark a class as a CoApi service. It provides
 * configuration for the service, including its name, base URL, and service ID.
 * This annotation is essential for integrating CoApi services with the Spring framework.
 *
 * When using this annotation, you can specify the following parameters:
 * - `serviceId`: The unique identifier for the service. Used to resolve the base URL if `baseUrl` is not provided.
 * - `baseUrl`: The base URL of the service.
 * Supports `lb://` for load-balanced services and `http://` for direct connections.
 * - `name`: The name of the CoApi instance. Defaults to the class name if not specified.
 *
 * Example usage:
 *  ```kotlin
 *  @CoApi(baseUrl = "https://api.github.com", name = "GitHubApi")
 *  interface GitHubApiClient {
 *      @GetExchange("repos/{owner}/{repo}/issues")
 *     fun getIssue(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
 *  }
 *  ```
 * ```kotlin
 * @CoApi(baseUrl = "\${github.url}")
 * interface GitHubApiClient {

 * }
 * ```
 * ```kotlin
 * @CoApi(serviceId = "github-service")
 * interface GitHubApiClient {

 * }
 * ```
 * ```kotlin
 * @CoApi(baseUrl = "lb://github-service")
 * interface GitHubApiClient {
 *
 * }
 * ```
 *
 * The `@CoApi` annotation automatically registers the annotated class as a Spring component,
 * allowing it to be managed and injected by the Spring IoC container.
 */
@Target(AnnotationTarget.CLASS)
@Component
annotation class CoApi(
    /**
     * The base URL of the CoApi. Supports protocols:
     * - `lb://` for load-balanced services. The `lb://` protocol will be resolved to `http://` internally.
     * - `http://` for direct HTTP connections.
     *
     * If not specified, the `baseUrl` will be resolved using the `serviceId` with the `lb://` protocol.
     *
     * Example:
     * - `lb://my-service` will be resolved to `http://my-service`
     * - `http://localhost:8080` will be used as the base URL.
     */
    val baseUrl: String = "",
    /**
     * The service ID of the CoApi. This ID is used to identify the service when resolving the base URL.
     * If the `baseUrl` is not specified, the `serviceId` will be used to construct a load balanced URL
     * with the `lb://` protocol.
     *
     * Example:
     * - If `serviceId` is "my-service" and `baseUrl` is not specified, the resolved URL will be "lb://my-service".
     */
    val serviceId: String = "",
    /**
     * The name of the CoApi. This name is used to uniquely identify the CoApi instance.
     * If not specified, the name will default to the simple name of the annotated class.
     *
     * Example:
     * - If the class is named `MyApi`, the default name will be "MyApi".
     * - If specified as `@CoApi(name = "customName")`, the name will be "customName".
     */
    val name: String = "",
)
