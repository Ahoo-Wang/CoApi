# CoApi

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://github.com/Ahoo-Wang/CoApi/blob/mvp/LICENSE)
[![GitHub release](https://img.shields.io/github/release/Ahoo-Wang/CoApi.svg)](https://github.com/Ahoo-Wang/CoApi/releases)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.ahoo.coapi/api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/me.ahoo.coapi/api)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/709bea2aec1d4cfd85991edf66b5ccbc)](https://app.codacy.com/gh/Ahoo-Wang/CoApi/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Codecov](https://codecov.io/gh/Ahoo-Wang/CoApi/graph/badge.svg?token=ayVd7lthB6)](https://codecov.io/gh/Ahoo-Wang/CoApi)
[![Integration Test Status](https://github.com/Ahoo-Wang/CoApi/actions/workflows/integration-test.yml/badge.svg)](https://github.com/Ahoo-Wang/CoApi)

In Spring Framework 6, a new HTTP client, [Spring6 HTTP Interface](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface), has been introduced. This interface allows developers to define HTTP services as Java interfaces using the `@HttpExchange` annotation.

However, automatic configuration support is not yet provided in the current version, and developers need to implement configurations manually.

While the *Spring* ecosystem already has [Spring Cloud OpenFeign](https://github.com/spring-cloud/spring-cloud-openfeign), it lacks support for the reactive programming model. To address this, *Spring Cloud OpenFeign* recommends an alternative solution, [feign-reactive](https://github.com/PlaytikaOSS/feign-reactive). However, this alternative is currently not actively maintained and does not support Spring Boot 3.2.x.

**CoApi** comes into play to address these challenges. It offers support for zero boilerplate code auto-configuration, similar to *Spring Cloud OpenFeign*. Developers only need to define interfaces, making usage straightforward.

## Installation

> Use *Gradle(Kotlin)* to install dependencies

```kotlin
implementation("me.ahoo.coapi:spring-boot-starter")
```

> Use *Gradle(Groovy)* to install dependencies

```groovy
implementation 'me.ahoo.coapi:spring-boot-starter'
```

> Use *Maven* to install dependencies

```xml
<dependency>
    <groupId>me.ahoo.coapi</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <version>${coapi.version}</version>
</dependency>
```

## Usage

### Define `CoApi` - a third-party interface

> `baseUrl` : Define the base address of the request, which can be obtained from the configuration file, for example: `baseUrl = "\{github.url}"`, `github.url` is the configuration item in the configuration file

```kotlin
@CoApi(baseUrl = "\${github.url}")
interface GitHubApiClient {

    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssue(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}
```

> Configuration：

```yaml
github:
  url: https://api.github.com
```

### Define `CoApi` - Client Load Balancing

```kotlin
@CoApi(serviceId = "github-service")
interface ServiceApiClient {

    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssue(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}
```

### Using `CoApi`

```kotlin
@RestController
class GithubController(
    private val gitHubApiClient: GitHubApiClient,
    private val serviceApiClient: ServiceApiClient
) {

    @GetMapping("/baseUrl")
    fun baseUrl(): Flux<Issue> {
        return gitHubApiClient.getIssue("Ahoo-Wang", "CoApi")
    }

    @GetMapping("/serviceId")
    fun serviceId(): Flux<Issue> {
        return serviceApiClient.getIssue("Ahoo-Wang", "CoApi")
    }
}
```

## Case Reference

[Example](./example)