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

import me.ahoo.coapi.api.CoApi
import me.ahoo.coapi.spring.AbstractCoApiRegistrar
import me.ahoo.coapi.spring.CoApiDefinition
import me.ahoo.coapi.spring.CoApiDefinition.Companion.toCoApiDefinition
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter

class AutoCoApiRegistrar : AbstractCoApiRegistrar() {

    override fun getCoApiDefinitions(importingClassMetadata: AnnotationMetadata): Set<CoApiDefinition> {
        val scanBasePackages = AutoConfigurationPackages.get(appContext).toSet()
        return scanBasePackages.toApiClientDefinitions()
    }

    private fun Set<String>.toApiClientDefinitions(): Set<CoApiDefinition> {
        val scanner = ApiClientScanner()
        return flatMap { basePackage ->
            scanner.findCandidateComponents(basePackage)
        }.map { beanDefinition ->
            Class.forName(beanDefinition.beanClassName).toCoApiDefinition(env)
        }.toSet()
    }
}

class ApiClientScanner : ClassPathScanningCandidateComponentProvider(false) {
    init {
        addIncludeFilter(AnnotationTypeFilter(CoApi::class.java))
    }

    override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {
        return beanDefinition.metadata.isInterface
    }
}
