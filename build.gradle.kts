plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.kotest)
    alias(libs.plugins.spotless)
    alias(libs.plugins.jib)
}

group = "com.bangbang93.openbmclapi.agent"

version = "0.0.1"

application { mainClass = "com.bangbang93.openbmclapi.agent.ApplicationKt" }

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
    filesMatching("version.properties") { expand("version" to project.version) }
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
    implementation(libs.weupnp)
    ksp(libs.koin.ksp.compiler)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}

kotlin { jvmToolchain(17) }

spotless {
    kotlin {
        ktfmt().metaStyle()
        targetExclude("build/generated/**/*")
    }
}

sonar {
    properties {
        property("sonar.projectKey", "bangbang93_openbmclapi.kt")
        property("sonar.organization", "bangbang93")
    }
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

// Jib 构建分层 Docker 镜像：自动把 dependencies / resources / classes 分成独立层，
// 依赖不变时层缓存命中，推送时跳过已存在的层；支持多平台 manifest list
jib {
    from {
        // 本地 base 镜像（含 tini），由 CI/本地先 docker build base/ 得到，不推送 registry
        image = "docker://openbmclapi-base:tools"
        // 默认 amd64，CI 的 arm64 job 用 -Djib.from.platforms=linux/arm64 覆盖
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
        }
    }
    to {
        // tag 由 CI 通过 -Djib.to.image=bangbang93/openbmclapi.kt:<tag> 覆盖
        image = "bangbang93/openbmclapi.kt"
        auth {
            username = providers.environmentVariable("DOCKER_HUB_USER").getOrElse("")
            password = providers.environmentVariable("DOCKER_HUB_PASS").getOrElse("")
        }
    }
    container {
        // tini 作为 PID 1，负责信号转发和回收僵尸进程；设置后 jvmFlags/mainClass 被忽略
        entrypoint = listOf(
            "tini", "--",
            "java",
            "-cp", "/app/classes:/app/resources:/app/libs/*",
            "com.bangbang93.openbmclapi.agent.ApplicationKt",
        )
        ports = listOf("4000")
        volumes = listOf("/app/cache")
        user = "10001:10001"
        environment = mapOf("CLUSTER_PORT" to "4000")
    }
}
