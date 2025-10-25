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
    mainClass = "com.bangbang93.openbmclapi.agent.ApplicationKt"
}

// 在打包时把项目 version 写入 JAR 的 manifest，方便运行时通过 Package.getImplementationVersion() 读取
tasks.withType<Jar> {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
        )
    }
}

// 在 resources 里生成一个 version.properties 模板，以便在未打包（如 gradle run / IDE 运行）时也能读取项目 version
tasks.processResources {
    filesMatching("version.properties") {
        expand("version" to project.version)
    }
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.koin.annotations)
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
    implementation(libs.avro4k.core)
    implementation(libs.zstd.jni)
    implementation(libs.sardine)
    implementation(libs.minio)
    implementation(libs.aliyun.oss)
    implementation(libs.kotlin.retry)
    implementation(libs.bouncycastle.prov)
    implementation(libs.bouncycastle.pkix)
    ksp(libs.koin.ksp.compiler)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.mockk)
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
