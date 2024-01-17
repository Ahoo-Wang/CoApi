plugins {
    alias(libs.plugins.kotlinSpring)
    kotlin("kapt")
}
dependencies {
    kapt(platform(project(":dependencies")))
    api(project(":spring"))
    api("org.springframework.boot:spring-boot-starter")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    kapt("org.springframework.boot:spring-boot-autoconfigure-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
}