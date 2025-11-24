plugins {
    alias(libs.plugins.apythia.kotlin.jvm.librarywithtesting)
    alias(libs.plugins.apythia.publishing)
}

dependencies {
    api(projects.http)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp.mockWebServer)

    testImplementation(projects.httpTesting)
    testImplementation(libs.okhttp.loggingInterceptor)
    testImplementation(libs.retrofit)
}
