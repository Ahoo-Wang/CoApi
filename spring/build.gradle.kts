java {
    registerFeature("lbSupport") {
        usingSourceSet(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
        capability(group.toString(), "lb-support", version.toString())
    }
}

dependencies {
    api(project(":api"))
    api("org.springframework:spring-context")
    api("org.springframework:spring-webflux")
    "lbSupportImplementation"("org.springframework.cloud:spring-cloud-commons")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}