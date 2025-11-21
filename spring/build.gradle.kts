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

java {
    registerFeature("reactiveSupport") {
        usingSourceSet(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
        capability(group.toString(), "reactive-support", version.toString())
    }
    registerFeature("lbSupport") {
        usingSourceSet(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
        capability(group.toString(), "lb-support", version.toString())
    }
    registerFeature("jwtSupport") {
        usingSourceSet(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
        capability(group.toString(), "jwt-support", version.toString())
    }
}

dependencies {
    api(project(":api"))
    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
    implementation("io.github.oshai:kotlin-logging-jvm")
    "reactiveSupportImplementation"("org.springframework.boot:spring-boot-webclient")
    "lbSupportImplementation"("org.springframework.cloud:spring-cloud-commons")
    "jwtSupportImplementation"(libs.java.jwt)
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    testImplementation(project(":example-provider-api"))
    testImplementation(project(":example-consumer-client"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}