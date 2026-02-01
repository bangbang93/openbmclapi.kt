package com.bangbang93.openbmclapi.agent.util

import io.socket.client.Ack
import io.socket.client.Socket
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject

suspend inline fun <reified T> Socket.emitAck(event: String, vararg args: T): Any? =
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
                CoroutineScope(Dispatchers.Default).launch {
                  cont.resumeWithException(Exception(err.toString()))
                }
              } else {
                CoroutineScope(Dispatchers.Default).launch { cont.resume(arg[1]) }
              }
            } catch (e: Exception) {
              CoroutineScope(Dispatchers.Default).launch { cont.resumeWithException(e) }
            }
          },
      )
    }

suspend inline fun <reified T> Socket.emitAck(event: String): T = suspendCoroutine { cont ->
  this.emit(
      event,
      emptyArray(),
      Ack { ackArgs: Array<Any?> ->
        try {
          val arg = ackArgs.firstOrNull() as? JSONArray ?: error("Invalid response")
          val err = arg[0]
          if (err != null && err != JSONObject.NULL) {
            CoroutineScope(Dispatchers.Default).launch {
              cont.resumeWithException(Exception(err.toString()))
            }
          } else {
            val res = arg[1] as JSONObject
            val ret = Json.decodeFromString<T>(res.toString())
            CoroutineScope(Dispatchers.Default).launch { cont.resume(ret) }
          }
        } catch (e: Exception) {
          CoroutineScope(Dispatchers.Default).launch { cont.resumeWithException(e) }
        }
      },
  )
}
