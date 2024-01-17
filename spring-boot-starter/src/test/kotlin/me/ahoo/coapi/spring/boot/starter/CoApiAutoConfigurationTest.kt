package me.ahoo.coapi.spring.boot.starter

import io.mockk.mockk
import me.ahoo.coapi.example.api.GitHubApiClient
import me.ahoo.coapi.example.api.ServiceApiClient
import me.ahoo.coapi.spring.EnableCoApi
import org.assertj.core.api.AssertionsForInterfaceTypes
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction

class CoApiAutoConfigurationTest {
    @Test
    fun `should create ApiClient bean`() {
        ApplicationContextRunner()
            .withPropertyValues("github.url=https://api.github.com")
            .withBean(LoadBalancedExchangeFilterFunction::class.java, { mockk() })
            .withUserConfiguration(WebClientAutoConfiguration::class.java)
            .withUserConfiguration(EnableCoApiConfiguration::class.java)
            .withUserConfiguration(CoApiAutoConfiguration::class.java)
            .run { context ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasSingleBean(GitHubApiClient::class.java)
                    .hasSingleBean(ServiceApiClient::class.java)

                context.getBean(GitHubApiClient::class.java)

                context.getBean(ServiceApiClient::class.java)
            }
    }
}

@SpringBootApplication
@EnableCoApi(apis = [GitHubApiClient::class, ServiceApiClient::class])
class EnableCoApiConfiguration
