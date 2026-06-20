plugins {
    `java-library`
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // CoApi starter - auto-configures @CoApi interfaces
    implementation("me.ahoo.coapi:coapi-spring-boot-starter")

    // Spring Boot WebFlux (provides WebClient for reactive mode)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Spring Boot RestClient (for sync mode)
    implementation("org.springframework.boot:spring-boot-starter-restclient")

    // Spring Cloud LoadBalancer for client-side load balancing with Consul
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")

    // Spring Cloud Consul for service discovery
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")

    // Jackson Kotlin module for JSON serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Reactor Kotlin extensions
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.2")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-parameters"))
}
