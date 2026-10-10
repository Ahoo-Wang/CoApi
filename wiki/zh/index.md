---
layout: home

hero:
  name: "CoApi"
  text: "Spring HTTP Interface 零样板客户端"
  tagline: 定义一个接口，添加注解，完成。支持响应式和同步模式，内置客户端负载均衡。
  actions:
    - theme: brand
      text: 快速开始
      link: /zh/getting-started/quick-start
    - theme: alt
      text: 什么是 CoApi？
      link: /zh/getting-started/overview
    - theme: alt
      text: GitHub
      link: https://github.com/Ahoo-Wang/CoApi

features:
  - title: "@CoApi 注解"
    details: 在 @HttpExchange 接口上标注 @CoApi 即可注入使用。HTTP 客户端和代理 Bean 由 CoApi 创建。
    link: /zh/deep-dive/annotations
  - title: "响应式与同步"
    details: 通过一个属性在 WebClient（响应式）和 RestClient（同步）之间切换。或者让 CoApi 从类路径自动推断。
    link: /zh/deep-dive/client-modes
  - title: "客户端负载均衡"
    details: 与 Spring Cloud LoadBalancer 集成。使用 serviceId、lb:// 或 @LoadBalanced 调用服务发现中的服务。
    link: /zh/deep-dive/load-balancing
  - title: "Spring Boot 自动配置"
    details: 添加 starter 后，应用包下的 @CoApi 接口会被自动发现。可通过 coapi.clients.* 按环境覆盖 URL。
    link: /zh/deep-dive/auto-configuration
  - title: "可定制"
    details: 通过 WebClientBuilderCustomizer 或 RestClientBuilderCustomizer 钩入客户端创建过程。通过配置为单个客户端挂载过滤器和拦截器。
    link: /zh/deep-dive/customization
  - title: "内置认证"
    details: BearerTokenFilter 配合共享令牌缓存，在 JWT 过期前自动刷新。
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
