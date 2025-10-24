package com.bangbang93.openbmclapi.agent

import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import com.bangbang93.openbmclapi.agent.model.Counters
import com.bangbang93.openbmclapi.agent.routes.clusterRoutes
import com.bangbang93.openbmclapi.agent.storage.IStorage
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.slf4j.event.*

fun Application.configureRouting() {
    val config by inject<ClusterConfig>()
    val storage by inject<IStorage>()
    val counters by inject<Counters>()

    install(AutoHeadResponse)
    routing {
        get("/") {
            call.respondText("OpenBMCLAPI Cluster - Kotlin Edition")
        }

        clusterRoutes(config, storage, counters)

        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
    }
}
