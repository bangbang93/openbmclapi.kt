plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ktlint)
}

group = "com.bangbang93.openbmclapi.agent"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)
    implementation(libs.kotlin.logging)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.auto.head.response)
    implementation(libs.ktor.server.partial.content)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.caching.headers)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.socket.io.client)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.dotenvKotlin)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

// Configure KSP to generate code in the correct source set
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

// Configure ktlint
ktlint {
    filter {
        exclude { entry ->
            entry.file.toString().contains("generated")
        }
    }
}
