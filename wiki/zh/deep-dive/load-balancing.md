---
title: 负载均衡
description: 使用 serviceId、lb:// 或 @LoadBalanced 调用服务发现中的服务，CoApi 如何接入 Spring Cloud LoadBalancer，以及如何按环境覆盖。
---

# 负载均衡

负载均衡客户端指向一个**服务 ID**，而不是具体主机。每次请求时，Spring Cloud LoadBalancer 从服务发现中挑选一个实例。

## 准备

1. 添加 `org.springframework.cloud:spring-cloud-starter-loadbalancer`（见[安装](../getting-started/installation.md#可选依赖)）。
2. 准备一个服务发现来源：Eureka、Consul、Kubernetes、Nacos，或 Spring Cloud 的静态 `SimpleDiscoveryClient`。
3. 把客户端标记为负载均衡。

## 标记客户端

以下任意一种即可：

```kotlin
@CoApi(serviceId = "order-service")
interface OrderClient

@CoApi(baseUrl = "lb://order-service")
interface OrderClient

@CoApi(baseUrl = "http://order-service")
@LoadBalanced
interface OrderClient
```

也可以通过配置按客户端开启或关闭：

```yaml
coapi:
  clients:
    OrderClient:
      load-balanced: true          # 或 false
      # base-url: lb://order-service   # lb:// 形式的 base-url 同样会启用
```

完整优先级见[覆盖规则](../getting-started/configuration.md#覆盖规则)。

## CoApi 做了什么

对于负载均衡客户端，CoApi 会：

1. 把 base URL 从 `lb://order-service` 改写为 `http://order-service`；
2. 向客户端的 Builder 添加 Spring Cloud 的负载均衡器：
   - 响应式：`LoadBalancedExchangeFilterFunction` Bean；
   - 同步：`BlockingLoadBalancerInterceptor` Bean（启用 Spring Retry 时也涵盖其重试变体）；
3. 如果 Builder 上已经有负载均衡过滤器或拦截器（例如由 Spring Cloud 自身添加的），则跳过第 2 步。负载均衡永远不会重复应用。

随后负载均衡器把 `order-service` 替换为所选实例的主机和端口。如果实例被标记为 secure，协议会切换为 `https`。

只有负载均衡客户端才会加载 Spring Cloud 的类，因此不使用负载均衡的应用完全不需要 Spring Cloud。

## 开发和测试中的静态实例

没有注册中心时，可以用 Spring Cloud 的 simple discovery client 在配置中列出实例：

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

## 按环境绕过负载均衡器

要在某个环境（例如预发环境）调用固定主机，覆盖 URL 即可。普通的 `base-url` 会关闭该客户端的负载均衡：

```yaml
coapi:
  clients:
    OrderClient:
      base-url: http://orders.staging.internal:8080
```

要保留注解中的 URL 但关闭负载均衡，设置 `load-balanced: false`。

## 失败情况

- classpath 上没有 Spring Cloud LoadBalancer：启动失败，报 `CoApi [OrderClient] is load balanced, but Spring Cloud LoadBalancer is not on the classpath`。
- 服务 ID 没有可用实例：请求在运行时失败，Spring Cloud 返回“无可用实例”/503 类错误。检查服务发现。

见[故障排查](../getting-started/troubleshooting.md)。
