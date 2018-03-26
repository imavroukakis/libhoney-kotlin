package io.honeycomb

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'")

fun toRfc3339(temporalAccessor: TemporalAccessor): String {
    return dateFormat.format(temporalAccessor)
}

fun toJsonString(event: Event): String = toJson(event).toJsonString()

fun toJsonString(marker: Marker): String = toJson(marker).toJsonString()

fun toJsonString(events: List<Event>): String = toJson(events).toJsonString()

private fun toJson(events: List<Event>): JsonArray<JsonObject> {
    val jsonArray = JsonArray<JsonObject>()
    for (event in events) {
        val jsonObject = JsonObject()
        jsonObject["time"] = toRfc3339(event.timeStamp)
        jsonObject["samplerate"] = event.sampleRate
        jsonObject["data"] = toJson(event)
        jsonArray.add(jsonObject)
    }
    return jsonArray
}

fun toJson(marker: Marker): JsonObject {
    val jsonObject = JsonObject()
    with(marker) {
        startTime.takeIf { it != -1L }.apply { jsonObject["start_time"] = this }
        endTime.takeIf { it != -1L }.apply { jsonObject["end_time"] = this }
        message.apply { jsonObject["message"] = this }
        type.apply { jsonObject["type"] = this }
        url.apply { jsonObject["url"] = this }
    }
    return jsonObject
}

fun toJson(event: Event): JsonObject = JsonObject(event.data)
