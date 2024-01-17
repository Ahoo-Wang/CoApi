plugins {
    application
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinSpring)
    kotlin("kapt")
}
dependencies {
    implementation(platform(project(":dependencies")))
    kapt(platform(project(":dependencies")))
    implementation("io.netty:netty-all")
    implementation(project(":example-api"))
    implementation(project(":spring-boot-starter"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-parameters"))
}