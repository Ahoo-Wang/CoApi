---
title: Load Balancing
description: Calling discovered services with serviceId, lb:// or @LoadBalanced, how CoApi wires Spring Cloud LoadBalancer, and how to override it per environment.
---

# Load Balancing

A load-balanced client addresses a **service ID** instead of a host. On each request, Spring Cloud LoadBalancer picks an instance from service discovery.

## Setup

1. Add `org.springframework.cloud:spring-cloud-starter-loadbalancer` (see [Installation](../getting-started/installation.md#optional-extras)).
2. Have a discovery source: Eureka, Consul, Kubernetes, Nacos, or Spring Cloud's static `SimpleDiscoveryClient`.
3. Mark the client as load balanced.

## Marking a client

Any one of these is enough:

```kotlin
@CoApi(serviceId = "order-service")
interface OrderClient

@CoApi(baseUrl = "lb://order-service")
interface OrderClient

@CoApi(baseUrl = "http://order-service")
@LoadBalanced
interface OrderClient
```

Configuration can also switch it on or off per client:

```yaml
coapi:
  clients:
    OrderClient:
      load-balanced: true          # or false
      # base-url: lb://order-service   # an lb:// base-url also enables it
```

The full precedence is in the [override rules](../getting-started/configuration.md#override-rules).

## What CoApi does

For a load-balanced client, CoApi:

1. rewrites the base URL from `lb://order-service` to `http://order-service`;
2. adds Spring Cloud's load balancer to the client's builder:
   - reactive: the `LoadBalancedExchangeFilterFunction` bean;
   - sync: the `BlockingLoadBalancerInterceptor` bean (this also covers the retrying variant when Spring Retry is enabled);
3. skips step 2 if the builder already carries a load-balancing filter or interceptor, such as one added by Spring Cloud itself. Load balancing is never applied twice.

The load balancer then replaces `order-service` with the chosen instance's host and port. If the instance is marked secure, it switches the scheme to `https`.

Spring Cloud classes are only loaded for load-balanced clients, so applications without load balancing do not need Spring Cloud at all.

## Static instances for development and tests

Without a registry, list instances in configuration with Spring Cloud's simple discovery client:

```yaml
spring:
  cloud:
    discovery:
      client:
        simple:
          instances:
            order-service:
              - host: localhost
                port: 8010
            github-service:
              - host: api.github.com
                port: 443
                secure: true
```

## Bypassing the load balancer per environment

To call a fixed host somewhere (for example in staging), override the URL. A plain `base-url` turns load balancing off for that client, unless `load-balanced: true` is also set for it (the explicit flag wins):

```yaml
coapi:
  clients:
    OrderClient:
      base-url: http://orders.staging.internal:8080
```

To keep the annotation's URL but turn load balancing off, set `load-balanced: false`.

## Failures

- No Spring Cloud LoadBalancer on the classpath: startup fails with `CoApi [OrderClient] is load balanced, but Spring Cloud LoadBalancer is not on the classpath`.
- No instances for the service ID: requests fail at runtime with a "no available instance"/503-style error from Spring Cloud. Check discovery.

See [Troubleshooting](../getting-started/troubleshooting.md).
