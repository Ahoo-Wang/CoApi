---
title: Installation
description: Add the CoApi starter, an HTTP client starter for your client mode, and optional load balancing and JWT support.
---

# Installation

A CoApi application needs three things on the classpath:

1. **The CoApi starter.**
2. **An HTTP client builder** for the [client mode](../deep-dive/client-modes.md): `WebClient` (reactive) or `RestClient` (sync).
3. **Optional extras**: Spring Cloud LoadBalancer for `serviceId`/`lb://` clients, and `java-jwt` for JWT token parsing.

Replace `<version>` (Maven: the `coapi.version` property) with the [latest release](https://github.com/Ahoo-Wang/CoApi/releases/latest) (see [version compatibility](./overview.md#version-compatibility)).

## CoApi starter

::: code-group

```kotlin [Gradle (Kotlin)]
implementation("me.ahoo.coapi:coapi-spring-boot-starter:<version>")
```

```groovy [Gradle (Groovy)]
implementation 'me.ahoo.coapi:coapi-spring-boot-starter:<version>'
```

```xml [Maven]
<dependency>
    <groupId>me.ahoo.coapi</groupId>
    <artifactId>coapi-spring-boot-starter</artifactId>
    <version>${coapi.version}</version>
</dependency>
```

:::

To manage all CoApi artifacts with one version, import the BOM `me.ahoo.coapi:coapi-bom` and drop the versions:

::: code-group

```kotlin [Gradle (Kotlin)]
implementation(platform("me.ahoo.coapi:coapi-bom:<version>"))
implementation("me.ahoo.coapi:coapi-spring-boot-starter")
```

```xml [Maven]
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>me.ahoo.coapi</groupId>
            <artifactId>coapi-bom</artifactId>
            <version>${coapi.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

:::

## HTTP client builder

CoApi builds each client from the application's `WebClient.Builder` or `RestClient.Builder` bean. The CoApi starter does **not** bring these in. In Spring Boot 4 they come from dedicated starters:

| Client mode | Add |
|-------------|-----|
| Reactive (`WebClient`) | `org.springframework.boot:spring-boot-starter-webclient` |
| Sync (`RestClient`) | `org.springframework.boot:spring-boot-starter-restclient` |

Without the matching builder, startup fails with `NoSuchBeanDefinitionException` for `WebClient.Builder` or `RestClient.Builder`.

::: warning AUTO mode follows the classpath
With the default `coapi.mode=AUTO`, CoApi uses `WebClient` whenever Spring WebFlux is on the classpath, even in a servlet application. If you need `RestClient` while WebFlux is present, set `coapi.mode=SYNC`. See [Client Modes](../deep-dive/client-modes.md).
:::

## Optional extras

**Load balancing** (`serviceId`, `lb://`, `@LoadBalanced`) needs Spring Cloud LoadBalancer and the Spring Cloud BOM matching your Spring Boot version:

```kotlin
implementation(platform("org.springframework.cloud:spring-cloud-dependencies:<spring-cloud-version>"))
implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
```

**JWT expiry parsing** (`String.jwtToExpirableToken()`, see [Authentication](../deep-dive/authentication.md)) needs `com.auth0:java-jwt`:

```kotlin
implementation("com.auth0:java-jwt:<version>")
```

## Artifacts

| Artifact | Contents | Use it when |
|----------|----------|-------------|
| `coapi-spring-boot-starter` | Auto-configuration and `coapi.*` properties | Spring Boot applications (the usual case) |
| `coapi-spring` | Registrars, factory beans, client SPI, auth filters | Plain Spring without Boot, using `@EnableCoApi` |
| `coapi-api` | `@CoApi` and `@LoadBalanced` only | API modules that declare clients but shouldn't depend on Spring Boot |
| `coapi-bom` | Version alignment for the artifacts above | Importing versions once |

The `coapi-api` split lets a provider publish a lightweight client module, like [`example-provider-api`](https://github.com/Ahoo-Wang/CoApi/tree/main/example/example-provider-api), that consumers pick up without inheriting Spring Boot.

## Next step

[Quick Start](./quick-start.md): define and call your first client.
