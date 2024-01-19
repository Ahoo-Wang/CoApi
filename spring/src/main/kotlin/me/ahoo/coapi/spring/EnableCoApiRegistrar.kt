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

package me.ahoo.coapi.spring

import me.ahoo.coapi.spring.CoApiDefinition.Companion.toCoApiDefinition
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotationMetadata

class EnableCoApiRegistrar : AbstractCoApiRegistrar() {

    @Suppress("UNCHECKED_CAST")
    override fun getCoApiDefinitions(importingClassMetadata: AnnotationMetadata): Set<CoApiDefinition> {
        val enableCoApi =
            importingClassMetadata.getAnnotationAttributes(EnableCoApi::class.java.name) ?: return emptySet()
        val apis = enableCoApi[EnableCoApi::apis.name] as Array<Class<*>>
        return apis.map { clientType ->
            clientType.toCoApiDefinition(env)
        }.toSet()
    }

    override fun setEnvironment(environment: Environment) {
        this.env = environment
    }
}
