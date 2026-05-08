# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CoApi is a Spring Framework library that provides zero-boilerplate auto-configuration for Spring 6 HTTP Interface clients (`@HttpExchange`). It supports both reactive (WebClient) and synchronous (RestClient) programming models, with optional client-side load balancing via Spring Cloud LoadBalancer.

- **Group ID**: `me.ahoo.coapi`
- **Language**: Kotlin (JVM 17 target)
- **Framework**: Spring Boot 4.x / Spring Framework 7.x
- **License**: Apache 2.0

## Build Commands

```bash
# Build all modules
./gradlew build

# Run tests for a specific module
./gradlew :spring:test
./gradlew :spring-boot-starter:test

# Run a single test class
./gradlew :spring:test --tests "me.ahoo.coapi.spring.CoApiDefinitionTest"

# Run a single test method
./gradlew :spring:test --tests "me.ahoo.coapi.spring.CoApiDefinitionTest.test method name"

# Code coverage
./gradlew :code-coverage-report:jacocoTestReport

# Static analysis (detekt)
./gradlew detekt

# Publish to local Maven
./gradlew publishToMavenLocal
```

## Module Structure

```
CoApi/
├── api/                    # Core annotations: @CoApi, @LoadBalanced
├── spring/                 # Spring integration: registrar, factory beans, client implementations
│   └── client/
│       ├── reactive/       # WebClient-based HttpExchangeAdapter (reactive)
│       └── sync/           # RestClient-based HttpExchangeAdapter (synchronous)
├── spring-boot-starter/    # Auto-configuration for Spring Boot
├── bom/                    # Bill of Materials (BOM)
├── dependencies/           # Dependency version management
├── example/                # Example applications
│   ├── example-provider-api/    # Shared API interfaces
│   ├── example-provider-server/ # Server implementation
│   ├── example-consumer-client/ # Client consuming the API
│   ├── example-consumer-server/ # Consumer application
│   └── example-sync/            # Synchronous client example
└── code-coverage-report/   # Aggregated JaCoCo coverage
```

## Architecture

### Registration Flow

1. `@CoApi` annotation on an interface marks it as an HTTP client (also acts as `@Component`)
2. `@EnableCoApi` or Spring Boot auto-configuration triggers classpath scanning
3. `AutoCoApiRegistrar` (boot) or `EnableCoApiRegistrar` discovers `@CoApi`-annotated interfaces
4. `CoApiRegistrar` registers two beans per interface:
   - An HTTP client bean (`WebClient` or `RestClient`) via `WebClientFactoryBean` / `RestClientFactoryBean`
   - A proxy bean via `CoApiFactoryBean` that uses Spring's `HttpServiceProxyFactory`

### Client Mode

`ClientMode` enum controls which HTTP client to use:
- `REACTIVE` — uses `WebClient`
- `SYNC` — uses `RestClient`
- `AUTO` (default) — infers from classpath (presence of `org.springframework.web.reactive.HandlerResult`)

Override via property: `coapi.mode=SYNC|REACTIVE|AUTO`

### Load Balancing

Two ways to enable load balancing:
1. `@CoApi(serviceId = "my-service")` — automatically uses `lb://` protocol
2. `@CoApi(baseUrl = "lb://my-service")` — explicit load-balanced URL
3. `@LoadBalanced` annotation on the interface

Requires `spring-cloud-starter-loadbalancer` on classpath.

### Key Classes

- `CoApiDefinition` — holds parsed metadata from `@CoApi` annotation (name, baseUrl, loadBalanced)
- `CoApiFactoryBean` — creates the HTTP service proxy for the annotated interface
- `HttpExchangeAdapterFactory` — SPI for creating `HttpExchangeAdapter` instances
- `AbstractHttpClientFactoryBean` — base for `WebClientFactoryBean` and `RestClientFactoryBean`

## Testing

- **Framework**: JUnit 5 with `fluent-assert` for assertions (`me.ahoo.test:fluent-assert-core`)
- **Mocking**: MockK
- **Test retry**: Tests auto-retry up to 2 times in CI (via `org.gradle.test-retry` plugin)
- **Logging**: Tests use Logback with config at `config/logback.xml`

## Static Analysis

Detekt is configured with `config/detekt/detekt.yml`. Key relaxations:
- `MaxLineLength`: 300 characters
- Many complexity/style rules disabled (LongParameterList, MagicNumber, etc.)
- Formatting uses ktlint via detekt-formatting plugin

## Compiler Flags

- `-Xjsr305=strict` — strict null-safety for JSR-305 annotations
- `-Xjvm-default=all-compatibility` — Java interoperability for Kotlin default methods
- `-parameters` — preserve parameter names in bytecode
