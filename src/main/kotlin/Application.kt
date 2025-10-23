package com.bangbang93.openbmclapi

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()
}
