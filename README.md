# CoApi

> [Documentation](https://coapi.ahoo.me/) | [中文文档](https://coapi.ahoo.me/zh/) | [中文 README](./README.zh-CN.md)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://github.com/Ahoo-Wang/CoApi/blob/main/LICENSE)
[![GitHub release](https://img.shields.io/github/release/Ahoo-Wang/CoApi.svg)](https://github.com/Ahoo-Wang/CoApi/releases)
[![Maven Central Version](https://img.shields.io/maven-central/v/me.ahoo.coapi/coapi-api)](https://central.sonatype.com/artifact/me.ahoo.coapi/coapi-api)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/709bea2aec1d4cfd85991edf66b5ccbc)](https://app.codacy.com/gh/Ahoo-Wang/CoApi/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Codecov](https://codecov.io/gh/Ahoo-Wang/CoApi/graph/badge.svg?token=ayVd7lthB6)](https://codecov.io/gh/Ahoo-Wang/CoApi)
[![Integration Test Status](https://github.com/Ahoo-Wang/CoApi/actions/workflows/integration-test.yml/badge.svg)](https://github.com/Ahoo-Wang/CoApi)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Ahoo-Wang/CoApi)

**Zero-boilerplate Spring HTTP Interface clients, reactive or synchronous.**

Spring's [HTTP Interface](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface) lets you declare an HTTP API as an `@HttpExchange` interface. You still have to build a client, an adapter and a proxy, and register a bean, for every interface. CoApi does that for you: annotate the interface with `@CoApi` and inject it.

- **Reactive or sync**: backed by `WebClient` or `RestClient`, chosen by `coapi.mode` or inferred from the classpath.
- **Client-side load balancing**: `serviceId`, `lb://` or `@LoadBalanced`, via Spring Cloud LoadBalancer.
- **Configurable per client**: override base URLs, load balancing, filters and interceptors with `coapi.clients.<name>.*`.
- **Extensible**: builder customizer beans, replaceable beans, and a refreshing bearer-token filter.

## Compatibility

| CoApi | Spring Boot | Spring Framework | JDK |
|-------|-------------|------------------|-----|
| 3.x | 4.x | 7.x | 17+ |
| 2.x | 4.x | 7.x | 17+ |
| 1.x | 3.2.x | 6.1.x | 17+ |

Upgrading from 2.x: [Migrating to 3.0](https://coapi.ahoo.me/getting-started/migration-v3).

## Installation

```kotlin
implementation("me.ahoo.coapi:coapi-spring-boot-starter:<version>")
// plus the builder for your client mode (Spring Boot 4):
implementation("org.springframework.boot:spring-boot-starter-webclient")   // reactive
// implementation("org.springframework.boot:spring-boot-starter-restclient") // sync
// optional, for serviceId / lb:// clients:
// implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
```

Maven, the BOM and optional dependencies: [Installation](https://coapi.ahoo.me/getting-started/installation).

## Usage

Declare the client in or below your `@SpringBootApplication` package:

```kotlin
@CoApi(baseUrl = "\${github.url}")
interface GitHubApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssues(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}
```

```yaml
github:
  url: https://api.github.com
```

Inject and call it:

```kotlin
@RestController
class IssueController(private val gitHubApiClient: GitHubApiClient) {
    @GetMapping("/issues")
    fun issues(): Flux<Issue> = gitHubApiClient.getIssues("Ahoo-Wang", "CoApi")
}
```

To call a discovered service instead, use `@CoApi(serviceId = "order-service")`. Synchronous clients use plain return types such as `List<Issue>`.

## Documentation

| | |
|-|-|
| [Quick Start](https://coapi.ahoo.me/getting-started/quick-start) | Reactive and sync clients step by step |
| [Configuration](https://coapi.ahoo.me/getting-started/configuration) | All `coapi.*` properties and override rules |
| [Load Balancing](https://coapi.ahoo.me/deep-dive/load-balancing) | `serviceId`, `lb://`, per-environment overrides |
| [Customization](https://coapi.ahoo.me/deep-dive/customization) | Filters, interceptors, builder customizers |
| [Troubleshooting](https://coapi.ahoo.me/getting-started/troubleshooting) | Startup errors and their fixes |
| [Examples](./example) | Runnable provider/consumer and sync applications |

## Contributing

See [CONTRIBUTING.md](./CONTRIBUTING.md). Security issues: [SECURITY.md](./SECURITY.md).

## License

[Apache License 2.0](./LICENSE)
