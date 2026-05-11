plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // CoApi Spring Boot Starter (auto-configuration for @CoApi interfaces)
    implementation("me.ahoo.coapi:spring-boot-starter:latest-SNAPSHOT")

    // Spring Boot WebFlux (reactive mode)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Spring Boot Web + RestClient (sync mode)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-restclient")

    // Spring Cloud Consul Discovery (service registration and discovery via Consul)
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")

    // Spring Cloud LoadBalancer (client-side load balancing)
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")

    // Jackson Kotlin module for JSON serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Kotlin coroutines reactor support
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
