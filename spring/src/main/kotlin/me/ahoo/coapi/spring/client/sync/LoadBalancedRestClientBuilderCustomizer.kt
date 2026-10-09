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

package me.ahoo.coapi.spring.client.sync

import me.ahoo.coapi.spring.CoApiDefinition
import org.springframework.beans.factory.BeanFactory
import org.springframework.cloud.client.loadbalancer.BlockingLoadBalancerInterceptor
import org.springframework.cloud.client.loadbalancer.DeferringLoadBalancerInterceptor
import org.springframework.web.client.RestClient

/**
 * Adds Spring Cloud's [BlockingLoadBalancerInterceptor] (including its retry variant), unless the builder
 * already carries a load-balancing interceptor (e.g. Spring Cloud's own deferring one).
 *
 * Only loaded when a client is load balanced, so Spring Cloud stays an optional dependency.
 */
internal class LoadBalancedRestClientBuilderCustomizer(
    private val beanFactory: BeanFactory
) : RestClientBuilderCustomizer {
    companion object {
        const val INTERCEPTOR_CLASS_NAME = "org.springframework.cloud.client.loadbalancer.BlockingLoadBalancerInterceptor"
    }

    override fun customize(coApiDefinition: CoApiDefinition, builder: RestClient.Builder) {
        builder.requestInterceptors { interceptors ->
            val hasLoadBalancedInterceptor = interceptors.any { interceptor ->
                interceptor is BlockingLoadBalancerInterceptor || interceptor is DeferringLoadBalancerInterceptor
            }
            if (!hasLoadBalancedInterceptor) {
                interceptors.add(beanFactory.getBean(BlockingLoadBalancerInterceptor::class.java))
            }
        }
    }
}
