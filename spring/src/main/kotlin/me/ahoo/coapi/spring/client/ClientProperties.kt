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

package me.ahoo.coapi.spring.client

import me.ahoo.coapi.spring.CoApiDefinition

/**
 * Per-client endpoint overrides of what the `@CoApi` annotation declares, keyed by [CoApiDefinition.name].
 *
 * Mode-specific settings live in [me.ahoo.coapi.spring.client.reactive.ReactiveClientProperties] and
 * [me.ahoo.coapi.spring.client.sync.SyncClientProperties]. Every one of these beans is optional.
 */
interface ClientProperties {

    /**
     * The configured base URL, or blank when not configured.
     */
    fun getBaseUri(coApiName: String): String

    /**
     * The configured load-balancing flag, or `null` when not configured.
     */
    fun getLoadBalanced(coApiName: String): Boolean?

    /**
     * Applies these overrides to [definition], see [CoApiDefinition.withOverrides].
     */
    fun resolve(definition: CoApiDefinition): CoApiDefinition {
        return definition.withOverrides(
            baseUrl = getBaseUri(definition.name),
            loadBalanced = getLoadBalanced(definition.name)
        )
    }

    /**
     * No overrides: every client is defined by its annotation alone.
     */
    object Empty : ClientProperties {
        override fun getBaseUri(coApiName: String): String = ""
        override fun getLoadBalanced(coApiName: String): Boolean? = null
    }
}
