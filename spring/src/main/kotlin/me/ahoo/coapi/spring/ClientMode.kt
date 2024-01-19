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

enum class ClientMode {
    REACTIVE, SYNC, AUTO;

    companion object {
        const val COAPI_CLIENT_MODE_PROPERTY = "coapi.mode"
        private const val REACTIVE_WEB_APPLICATION_CLASS = "org.springframework.web.reactive.HandlerResult"
        private val INFERRED_MODE_BASED_ON_CLASS: ClientMode by lazy {
            try {
                Class.forName(REACTIVE_WEB_APPLICATION_CLASS)
                REACTIVE
            } catch (ignore: ClassNotFoundException) {
                SYNC
            }
        }

        fun inferClientMode(getProperty: (propertyKey: String) -> String?): ClientMode {
            val propertyValue = getProperty(COAPI_CLIENT_MODE_PROPERTY) ?: AUTO.name
            val mode = ClientMode.valueOf(propertyValue.uppercase())
            if (mode == AUTO) {
                return INFERRED_MODE_BASED_ON_CLASS
            }
            return mode
        }
    }
}
