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
import me.ahoo.coapi.spring.CoApiDefinition.Companion.LB_PROTOCOL_PREFIX
import org.springframework.context.ApplicationContext

interface IHttpClientFactoryBean {
    val definition: CoApiDefinition
    val appContext: ApplicationContext

    fun getBaseUrl(): String {
        val clientProperties = appContext.getBean(ClientProperties::class.java)
        return clientProperties.getBaseUri(definition.name).ifBlank {
            definition.baseUrl
        }
    }

    fun loadBalanced(): Boolean {
        return getBaseUrl().startsWith(LB_PROTOCOL_PREFIX) || definition.loadBalanced
    }
}
