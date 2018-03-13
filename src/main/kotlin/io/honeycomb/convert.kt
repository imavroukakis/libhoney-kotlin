package io.honeycomb

import com.beust.klaxon.JsonObject

fun toJsonString(event: Event): String = toJson(event).toJsonString()

fun toJson(event: Event): JsonObject = JsonObject(event.data)
