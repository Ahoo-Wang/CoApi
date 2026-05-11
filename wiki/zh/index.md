---
layout: home

hero:
  name: "CoApi"
  text: "Spring 6 零样板 HTTP 客户端"
  tagline: 定义一个接口，添加注解，完成。支持响应式和同步模式，内置客户端负载均衡。
  actions:
    - theme: brand
      text: 快速开始
      link: /zh/getting-started/quick-start
    - theme: alt
      text: 架构
      link: /zh/deep-dive/architecture
    - theme: alt
      text: GitHub
      link: https://github.com/Ahoo-Wang/CoApi

features:
  - title: "@CoApi 注解"
    details: 在任何接口上标记 @CoApi 注解 — CoApi 自动配置 HTTP 客户端、代理 bean 及所有支持基础设施。零样板代码。
    link: /zh/deep-dive/annotations
  - title: "响应式与同步"
    details: 通过一个属性在 WebClient（响应式）和 RestClient（同步）之间切换。或者让 CoApi 从类路径自动推断。
    link: /zh/deep-dive/client-modes
  - title: "客户端负载均衡"
    details: 与 Spring Cloud LoadBalancer 集成。使用 serviceId 或 lb:// 协议实现微服务间的自动实例选择。
    link: /zh/deep-dive/load-balancing
  - title: "Spring Boot 自动配置"
    details: 添加 starter 依赖后，@CoApi 接口通过类路径扫描自动被发现。无需手动配置。
    link: /zh/deep-dive/auto-configuration
  - title: "可自定义的 SPI"
    details: 通过 WebClientBuilderCustomizer 或 RestClientBuilderCustomizer 钩入客户端创建过程。为每个客户端配置过滤器、拦截器和连接池。
    link: /zh/deep-dive/customization
  - title: "内置认证"
    details: 带 JWT 感知响应式令牌缓存的 BearerTokenFilter。令牌获取一次后缓存，并在过期时自动刷新。
    link: /zh/deep-dive/authentication
---

<script setup>
</script>

<style>
:root {
  --vp-home-hero-name-color: transparent;
  --vp-home-hero-name-background: -webkit-linear-gradient(120deg, #6d5dfc 30%, #a78bfa);
  --vp-home-hero-image-background-image: linear-gradient(-45deg, #6d5dfc33 50%, #a78bfa33 50%);
  --vp-home-hero-image-filter: blur(44px);
}
</style>
