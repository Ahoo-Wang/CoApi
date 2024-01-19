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

package me.ahoo.coapi.spring.boot.starter

import me.ahoo.coapi.spring.ClientMode
import me.ahoo.coapi.spring.client.ClientProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

const val COAPI_PREFIX = "coapi"
const val ENABLED_SUFFIX_KEY = ".enabled"

@ConfigurationProperties(prefix = COAPI_PREFIX)
data class CoApiProperties(
    @DefaultValue("true") var enabled: Boolean = true,
    val mode: ClientMode = ClientMode.AUTO,
    val clients: Map<String, ClientDefinition> = emptyMap(),
) : ClientProperties {
    override fun getFilter(coApiName: String): ClientProperties.FilterDefinition {
        return clients[coApiName]?.reactive?.filter ?: ClientProperties.FilterDefinition()
    }

    override fun getInterceptor(coApiName: String): ClientProperties.InterceptorDefinition {
        return clients[coApiName]?.sync?.interceptor ?: ClientProperties.InterceptorDefinition()
    }
}

data class ClientDefinition(
    var reactive: ReactiveClientDefinition = ReactiveClientDefinition(),
    var sync: SyncClientDefinition = SyncClientDefinition()
)

data class ReactiveClientDefinition(
    var filter: ClientProperties.FilterDefinition = ClientProperties.FilterDefinition()
)

data class SyncClientDefinition(
    var interceptor: ClientProperties.InterceptorDefinition = ClientProperties.InterceptorDefinition()
)
