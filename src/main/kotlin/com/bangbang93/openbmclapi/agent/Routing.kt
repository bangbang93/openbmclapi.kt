package com.bangbang93.openbmclapi.agent

import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import com.bangbang93.openbmclapi.agent.model.Counters
import com.bangbang93.openbmclapi.agent.routes.clusterRoutes
import com.bangbang93.openbmclapi.agent.storage.IStorage
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
  val config by inject<ClusterConfig>()
  val storage by inject<IStorage>()
  val counters by inject<Counters>()

  install(AutoHeadResponse)
  routing {
    get("/") { call.respondText("OpenBMCLAPI Cluster - Kotlin Edition") }

    clusterRoutes(config, storage, counters)
  }
}
