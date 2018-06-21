package io.honeycomb

import java.time.LocalDateTime
import java.time.temporal.TemporalAccessor

class Event private constructor(
    val writeKey: String,
    val dataSet: String,
    val apiHost: String = "https://api.honeycomb.io",
    val sampleRate: Int = 1,
    val timeStamp: LocalDateTime = LocalDateTime.now(),
    val data: Map<String, Any> = mapOf()
) {

    companion object {
        /**
         * Creates a new event
         *
         * @param writeKey the event's Writekey
         * @param dataSet the dataset this event belongs in
         * @param apiHost an optional endpoint for the event to be transmitted to, defaults to `https://api.honeycomb.io`
         * @param sampleRate the sampling rate for this event, defaults to `1`
         * @param timeStamp the timestamp of this event, defaults to [LocalDateTime.now]
         *
         * @return a new [Event]
         */
        fun newEvent(
            writeKey: String,
            dataSet: String,
            apiHost: String = "https://api.honeycomb.io",
            sampleRate: Int = 1,
            timeStamp: LocalDateTime = LocalDateTime.now()
        ) =
            Event(writeKey, dataSet, apiHost, sampleRate, timeStamp)

        /**
         * Creates a new event, initialising it from a [HoneyConfig] instance
         *
         * @param honeyConfig the [HoneyConfig] instance to initialise this event with
         * @param timeStamp the timestamp of this event, defaults to [LocalDateTime.now]
         *
         * @return a new [Event]
         */
        fun newEvent(honeyConfig: HoneyConfig, timeStamp: LocalDateTime = LocalDateTime.now()) =
            Event(
                honeyConfig.writeKey, honeyConfig.dataSet,
                honeyConfig.apiHost, honeyConfig.sampleRate,
                timeStamp
            )
    }

    private constructor (event: Event, eventDataPair: Pair<String, Any>)
        : this(
        event.writeKey,
        event.dataSet,
        event.apiHost,
        event.sampleRate,
        event.timeStamp,
        event.data.plus(eventDataPair)
    )

    private constructor (event: Event, events: Map<String, Any>)
        : this(event.writeKey, event.dataSet, event.apiHost, event.sampleRate, event.timeStamp, event.data.plus(events))

    /**
     * Adds a key/value pair to this event. This function can be chained for a more fluent style
     *
     * @return a new [Event] instance
     */
    fun add(attribute: String, value: Any): Event {
        return when (value) {
            is TemporalAccessor ->
                Event(this, attribute to value.toRfc3339())
            is LongRange ->
                Event(this, attribute to listOf(value).flatten())
            is IntRange ->
                Event(this, attribute to listOf(value).flatten())
            is Collection<*> ->
                Event(this, attribute to listOf(value).flatten())
            is Pair<*, *> ->
                Event(this, attribute to value.toList())
            else ->
                Event(this, attribute to value)
        }
    }

    /**
     * Adds the contents of [events] to this event. This function can be chained for a more fluent style
     *
     * @return a new [Event] instance
     */
    fun add(events: Map<String, Any>): Event {
        return Event(this, data.plus(events))
    }
}
