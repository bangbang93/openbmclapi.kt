package com.bangbang93.openbmclapi.agent.util

import io.socket.client.Socket
import kotlinx.serialization.json.Json
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend inline fun <reified T> Socket.emitAck(
    event: String,
    vararg args: T,
): Any? =
    suspendCoroutine { cont ->
        val jsonArgs = args.map { JSONObject(Json.encodeToString<T>(it)) }.toTypedArray()
        this.emit(
            event,
            jsonArgs,
        ) { args: Array<Any> ->
            val err = args.firstOrNull()
            if (err != null) {
                cont.resumeWithException(Exception(err.toString()))
            } else {
                cont.resume(args[1])
            }
        }
    }
