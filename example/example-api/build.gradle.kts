
dependencies {
    implementation(platform(project(":dependencies")))
    implementation(project(":api"))
    implementation("io.projectreactor:reactor-core")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-webflux")
}