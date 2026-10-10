---
title: Quick Start
description: Define a CoApi client interface, configure its base URL, and inject it, in reactive (WebClient) or synchronous (RestClient) style.
---

# Quick Start

This page assumes a Spring Boot application with the [dependencies installed](./installation.md).

## 1. Declare the client

Write an interface with Spring's `@HttpExchange` annotations and mark it with `@CoApi`. Put it in (or below) the package of your `@SpringBootApplication` class so it gets discovered automatically.

::: code-group

```kotlin [Reactive (Kotlin)]
@CoApi(baseUrl = "\${github.url}")
interface GitHubApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    fun getIssues(@PathVariable owner: String, @PathVariable repo: String): Flux<Issue>
}

data class Issue(val url: String)
```

```java [Sync (Java)]
@CoApi(baseUrl = "${github.url}")
public interface GitHubApiClient {
    @GetExchange("repos/{owner}/{repo}/issues")
    List<Issue> getIssues(@PathVariable String owner, @PathVariable String repo);
}

record Issue(String url) {} // package-private, so both types fit in one file
```

:::

In Kotlin, escape the placeholder as `\${...}` so it isn't treated as string interpolation.

## 2. Configure the base URL

```yaml
github:
  url: https://api.github.com
```

The placeholder must resolve, or startup fails with `Could not resolve placeholder 'github.url'`. Use `${github.url:https://api.github.com}` to give it a default.

## 3. Inject and call

::: code-group

```kotlin [Reactive (Kotlin)]
@RestController
class IssueController(private val gitHubApiClient: GitHubApiClient) {
    @GetMapping("/issues")
    fun issues(): Flux<Issue> = gitHubApiClient.getIssues("Ahoo-Wang", "CoApi")
}
```

```java [Sync (Java)]
@RestController
public class IssueController {
    private final GitHubApiClient gitHubApiClient;

    public IssueController(GitHubApiClient gitHubApiClient) {
        this.gitHubApiClient = gitHubApiClient;
    }

    @GetMapping("/issues")
    public List<Issue> issues() {
        return gitHubApiClient.getIssues("Ahoo-Wang", "CoApi");
    }
}
```

:::

That's it. CoApi registered a `GitHubApiClient.HttpClient` bean (the `WebClient` or `RestClient`) and a `GitHubApiClient.CoApi` bean (the proxy you injected).

## Calling a discovered service

To call a service registered in service discovery, use `serviceId` instead of `baseUrl`, and add `spring-cloud-starter-loadbalancer`:

```kotlin
@CoApi(serviceId = "order-service")
interface OrderClient {
    @GetExchange("orders/{id}")
    fun getOrder(@PathVariable id: String): Mono<Order>
}
```

See [Load Balancing](../deep-dive/load-balancing.md).

## Where to go next

| I want to... | Read |
|--------------|------|
| Register clients from another package or JAR | [Registering Clients](../deep-dive/auto-configuration.md) |
| Override a URL per environment | [Configuration Reference](./configuration.md) |
| Add headers, auth, timeouts or connection pools | [Customization](../deep-dive/customization.md), [Authentication](../deep-dive/authentication.md) |
| Understand a startup error | [Troubleshooting](./troubleshooting.md) |
