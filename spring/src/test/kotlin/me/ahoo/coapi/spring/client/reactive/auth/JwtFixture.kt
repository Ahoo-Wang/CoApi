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

package me.ahoo.coapi.spring.client.reactive.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtFixture {
    var ALGORITHM = Algorithm.HMAC256("FyN0Igd80Gas8stTavArGKOYnS9uLWGA_")

    fun generateToken(expiresAt: Date): String {
        val accessTokenBuilder = JWT.create()
            .withExpiresAt(expiresAt)
        return accessTokenBuilder.sign(ALGORITHM)
    }
}
