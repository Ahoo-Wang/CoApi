---
layout: home

hero:
  name: "CoApi"
  text: "Zero-boilerplate HTTP Interface Clients for Spring"
  tagline: Define an interface, annotate it, done. Reactive & synchronous, with client-side load balancing.
  actions:
    - theme: brand
      text: Quick Start
      link: /getting-started/quick-start
    - theme: alt
      text: What is CoApi?
      link: /getting-started/overview
    - theme: alt
      text: GitHub
      link: https://github.com/Ahoo-Wang/CoApi

features:
  - title: "@CoApi Annotation"
    details: Mark an @HttpExchange interface with @CoApi and inject it. CoApi builds the HTTP client and the proxy bean for you.
    link: /deep-dive/annotations
  - title: "Reactive & Synchronous"
    details: Switch between WebClient (reactive) and RestClient (synchronous) with a single property. Or let CoApi infer from your classpath.
    link: /deep-dive/client-modes
  - title: "Client-Side Load Balancing"
    details: Integrated with Spring Cloud LoadBalancer. Use serviceId, lb:// or @LoadBalanced to call discovered services.
    link: /deep-dive/load-balancing
  - title: "Spring Boot Auto-Configuration"
    details: Add the starter and @CoApi interfaces in your application packages are discovered automatically. Override URLs per environment with coapi.clients.*.
    link: /deep-dive/auto-configuration
  - title: "Customizable"
    details: Hook into client creation with WebClientBuilderCustomizer or RestClientBuilderCustomizer. Attach filters and interceptors per client from configuration.
    link: /deep-dive/customization
  - title: "Built-in Auth"
    details: BearerTokenFilter with a shared token cache that refreshes JWTs before they expire.
    link: /deep-dive/authentication
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
