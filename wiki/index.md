---
layout: home

hero:
  name: "CoApi"
  text: "Zero-boilerplate HTTP Client for Spring 6"
  tagline: Define an interface, add annotations, done. Supports reactive and sync modes with built-in client-side load balancing.
  actions:
    - theme: brand
      text: Quick Start
      link: /getting-started/quick-start
    - theme: alt
      text: Architecture
      link: /deep-dive/architecture
    - theme: alt
      text: GitHub
      link: https://github.com/Ahoo-Wang/CoApi

features:
  - title: "@CoApi Annotation"
    details: Mark any interface with @CoApi — CoApi auto-configures HTTP client, proxy bean, and all supporting infrastructure. Zero boilerplate.
    link: /deep-dive/annotations
  - title: "Reactive & Sync"
    details: Switch between WebClient (reactive) and RestClient (sync) with a single property. Or let CoApi auto-infer from classpath.
    link: /deep-dive/client-modes
  - title: "Client-side Load Balancing"
    details: Integrated with Spring Cloud LoadBalancer. Use serviceId or lb:// protocol for automatic instance selection between microservices.
    link: /deep-dive/load-balancing
  - title: "Spring Boot Auto-configuration"
    details: Add starter dependency, @CoApi interfaces are auto-discovered via classpath scanning. No manual configuration needed.
    link: /deep-dive/auto-configuration
  - title: "Customizable SPI"
    details: Hook into client creation via WebClientBuilderCustomizer or RestClientBuilderCustomizer. Configure filters, interceptors, and connection pools per client.
    link: /deep-dive/customization
  - title: "Built-in Authentication"
    details: BearerTokenFilter with JWT-aware reactive token caching. Tokens fetched once, cached, and auto-refreshed on expiration.
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
