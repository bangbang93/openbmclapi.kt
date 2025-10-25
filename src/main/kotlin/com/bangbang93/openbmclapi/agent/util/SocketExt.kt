package com.bangbang93.openbmclapi.agent.util

import io.socket.client.Ack
import io.socket.client.Socket
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend inline fun <reified T> Socket.emitAck(
    event: String,
    vararg args: T,
): Any? =
    suspendCoroutine { cont ->
        // socket.io的参数只认JSONObject
        val jsonArgs = args.map { JSONObject(Json.encodeToString<T>(it)) }.toTypedArray()
        this.emit(
            event,
            *jsonArgs,
            Ack { ackArgs: Array<Any?> ->
                try {
                    val arg = ackArgs.firstOrNull() as? JSONArray ?: return@Ack cont.resume(null)
                    val err = arg[0]
                    if (err != null) {
                        cont.resumeWithException(Exception(err.toString()))
                    } else {
                        cont.resume(arg[1])
                    }
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            },
        )
    }
