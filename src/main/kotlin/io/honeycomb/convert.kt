package io.honeycomb

import com.beust.klaxon.JsonObject

fun toJsonString(event: Event): String = toJson(event).toJsonString()

fun toJsonString(marker: Marker): String = toJson(marker).toJsonString()

fun toJson(event: Event): JsonObject = JsonObject(event.data)

fun toJson(marker: Marker): JsonObject {
    var jsonObject = JsonObject()
    with(marker) {
        startTime?.apply { jsonObject.put("start_time", this) }
        endTime?.apply { jsonObject.put("end_time", this) }
        message.apply { jsonObject.put("message", this) }
        type.apply { jsonObject.put("type", this) }
        url.apply { jsonObject.put("url", this) }
    }
    return jsonObject
}
