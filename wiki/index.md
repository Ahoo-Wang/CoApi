---
layout: home

hero:
  name: "CoApi"
  text: "Zero-boilerplate HTTP Clients for Spring 6"
  tagline: Define an interface, annotate it, done. Reactive & synchronous, with client-side load balancing.
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
    details: Mark any interface with @CoApi — CoApi auto-configures the HTTP client, proxy bean, and all supporting infrastructure. Zero boilerplate.
    link: /deep-dive/annotations
  - title: "Reactive & Synchronous"
    details: Switch between WebClient (reactive) and RestClient (synchronous) with a single property. Or let CoApi infer from your classpath.
    link: /deep-dive/client-modes
  - title: "Client-Side Load Balancing"
    details: Integrated with Spring Cloud LoadBalancer. Use serviceId or lb:// protocol for automatic instance selection across microservices.
    link: /deep-dive/load-balancing
  - title: "Spring Boot Auto-Configuration"
    details: Add the starter dependency and @CoApi interfaces are auto-discovered via classpath scanning. No manual setup required.
    link: /deep-dive/auto-configuration
  - title: "Customizable SPI"
    details: Hook into client creation with WebClientBuilderCustomizer or RestClientBuilderCustomizer. Configure filters, interceptors, and connection pools per client.
    link: /deep-dive/customization
  - title: "Built-in Auth"
    details: BearerTokenFilter with JWT-aware reactive token caching. Token is fetched once, cached, and automatically refreshed on expiry.
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
