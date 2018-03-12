import com.beust.klaxon.JsonObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import java.time.temporal.TemporalAccessor


class Event(
        val writeKey: String,
        val dataSet: String,
        val apiHost: String = "https://api.honeycomb.io",
        val sampleRate: Int = 1,
        val timeStamp: LocalDateTime = LocalDateTime.now(),
        val data: Map<String, Any> = mapOf()) {


    constructor(honeyConfig: HoneyConfig, timeStamp: LocalDateTime = LocalDateTime.now())
            : this(honeyConfig.writeKey, honeyConfig.dataSet, honeyConfig.apiHost, honeyConfig.sampleRate, timeStamp)

    private constructor (event: Event, dataPair: Pair<String, Any>)
            : this(event.writeKey, event.dataSet, event.apiHost, event.sampleRate, event.timeStamp, event.data.plus(dataPair))

    fun add(attribute: String, value: Any): Event {
        return when (value) {
            is TemporalAccessor ->
                Event(this, attribute to ISO_LOCAL_DATE_TIME.format(value))
            is LongRange ->
                Event(this, attribute to listOf(value).flatten())
            is IntRange ->
                Event(this, attribute to listOf(value).flatten())
            else ->
                Event(this, attribute to value)
        }
    }

    fun toJsonString(): String {
        return toJson().toJsonString()
    }

    fun toJson(): JsonObject {
        return JsonObject(data)
    }
}
