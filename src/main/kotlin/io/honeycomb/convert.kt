package io.honeycomb

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

internal val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'")

fun TemporalAccessor.toRfc3339(): String = dateFormat.format(this)

fun List<Event>.toJsonString(): String = toJsonArray(this).toJsonString()

fun Marker.toJson(): JsonObject {
    val jsonObject = JsonObject()
    with(this) {
        startTime.takeIf { it != -1L }.apply { jsonObject["start_time"] = this }
        endTime.takeIf { it != -1L }.apply { jsonObject["end_time"] = this }
        message.apply { jsonObject["message"] = this }
        type.apply { jsonObject["type"] = this }
        url.apply { jsonObject["url"] = this }
    }
    return jsonObject
}

fun Marker.toJsonString(): String = toJson().toJsonString()

fun Event.toJsonString(): String = toJson().toJsonString()

fun Event.toJson(): JsonObject = JsonObject(this.data)

private fun toJsonArray(events: List<Event>): JsonArray<JsonObject> {
    val jsonArray = JsonArray<JsonObject>()
    for (event in events) {
        val jsonObject = JsonObject()
        jsonObject["time"] = event.timeStamp.toRfc3339()
        jsonObject["samplerate"] = event.sampleRate
        jsonObject["data"] = event.toJson()
        jsonArray.add(jsonObject)
    }
    return jsonArray
}
