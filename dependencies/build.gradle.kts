dependencies {
    api(platform(libs.springBootDependencies))
    api(platform(libs.springCloudDependencies))
    constraints {
        api(libs.hamcrest)
        api(libs.mockk)
        api(libs.detektFormatting)
    }
}