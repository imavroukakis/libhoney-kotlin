package io.honeycomb

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.core.Method.*
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import mu.KotlinLogging

object Transmit {

    private val logger = KotlinLogging.logger {}
    private const val HEADER_HONEYCOMB_TEAM = "X-Honeycomb-Team"
    private const val HEADER_HONEYCOMB_EVENT_TIME = "X-Honeycomb-io.honeycomb.Event-Time"
    private const val HEADER_HONEYCOMB_SAMPLE_RATE = "X-Honeycomb-Samplerate"
    private const val EVENTS_PATH = "/1/events/"
    private const val BATCH_EVENTS_PATH = "/1/batch/"
    private const val MARKERS_PATH = "/1/markers/"

    init {
        FuelManager.instance.baseHeaders = mapOf(
                "Content-Type" to "application/json",
                "User-Agent" to "libhoney-kt/0.0.5"
        )
    }


    /**
     * Transmits an [Event] to the API. Optionally merges in [GlobalConfig] before transmission
     *
     * This is a _blocking_ request and you will need to handle the result
     *
     * @param event the [Event] to transmit
     * @return the response [Triple]
     */
    fun blockingSend(event: Event): Triple<Request, Response, Result<String, FuelError>> {
        val eventsWithGlobalPairs = GlobalConfig.applyFields(event)
        return eventRequest(
                "${eventsWithGlobalPairs.apiHost}$EVENTS_PATH${eventsWithGlobalPairs.dataSet}",
                eventsWithGlobalPairs
        ).responseString()
    }

    /**
     * Batch transmits events to the API. Optionally merges in [GlobalConfig] before transmission
     *
     * This is a _blocking_ request and you will need to handle the result
     *
     * @param events the [List] of [Event] to transmit
     * @return the response [Triple]
     */
    fun blockingSend(events: List<Event>, honeyConfig: HoneyConfig): Triple<Request, Response, Result<String, FuelError>> {
        val eventsWithGlobalPairs = events.map { GlobalConfig.applyFields(it) }
        return eventRequest(
                "${honeyConfig.apiHost}$BATCH_EVENTS_PATH${honeyConfig.dataSet}",
                honeyConfig,
                eventsWithGlobalPairs
        ).responseString()
    }

    /**
     * Transmits an [Event] to the API. Optionally merges in [GlobalConfig] before transmission
     *
     * This is an _async_ request. You can provide an optional handler, if you are interest in evaluating
     * the response.
     *
     * @param event the [Event] to transmit
     * @param handler an optional result handler
     */
    fun send(event: Event, handler: ((Request, Response, Result<String, FuelError>) -> Unit)? = null) {
        val evt = GlobalConfig.applyFields(event)
        val safeHandler: (Request, Response, Result<String, FuelError>) -> Unit = handler ?: { _, response, result ->
            result.fold({ _ ->
                logger.debug { response.statusCode }
            }, { err ->
                logger.warn { err }
            })
        }
        eventRequest("${evt.apiHost}$EVENTS_PATH${evt.dataSet}", evt).responseString(handler = safeHandler)
    }

    /**
     * Creates a new [Marker]
     *
     * This is a _blocking_ request and you will need to handle the result
     *
     * @param marker the [Marker] to transmit
     * @return the [Result], contains a new [Marker] instance additionally populated with the marker id or an exception
     * if the call failed
     */
    fun createMarker(marker: Marker, honeyConfig: HoneyConfig): Result<Marker, Exception> {
        val (_, _, result) = markerRequest(marker, Method.POST, honeyConfig).responseString()
        return when (result) {
            is Result.Failure -> {
                Result.error(result.error)
            }
            is Result.Success -> {
                Result.of(Klaxon().parse<Marker>(result.get()))
            }
        }
    }


    /**
     * Updates an existing [Marker]
     *
     * This is a _blocking_ request and you will need to handle the result
     *
     * @param marker the modified [Marker]
     *
     * @return the [Result], contains a new [Marker] instance with the update marker data or an exception
     * if the call failed
     */
    fun updateMarker(marker: Marker, honeyConfig: HoneyConfig): Result<Marker, Exception> {
        val (_, _, result) = markerRequest(marker, Method.PUT, honeyConfig).responseString()
        return when (result) {
            is Result.Failure -> {
                Result.error(result.error)
            }
            is Result.Success -> {
                Result.of(Klaxon().parse<Marker>(result.get()))
            }
        }
    }

    /**
     * Deletes an existing [Marker]
     *
     * This is a _blocking_ request and you will need to handle the result
     *
     * @param marker the [Marker] to delete
     *
     * @return the [Result], contains the deleted [Marker] instance or an exception if the call failed
     */
    fun removeMarker(marker: Marker, honeyConfig: HoneyConfig): Result<Marker, Exception> {
        val (_, _, result) = markerRequest(marker, DELETE, honeyConfig).responseString()
        return when (result) {
            is Result.Failure -> {
                Result.error(result.error)
            }
            is Result.Success -> {
                Result.of(Klaxon().parse<Marker>(result.get()))
            }
        }
    }


    /**
     * Lists all existing [Marker]
     *
     * This is a _blocking_ request and you will need to handle the result
     *
     * @param honeyConfig the [HoneyConfig]
     *
     * @return the [Result], contains a list of [Marker] instances or an exception if the call failed
     */
    fun allMarkers(honeyConfig: HoneyConfig): Result<List<Marker>, Exception> {
        val (_, _, result) = markerRequest(honeyConfig = honeyConfig, method = GET).responseString()
        return when (result) {
            is Result.Failure -> {
                Result.error(result.error)
            }
            is Result.Success -> {
                Result.of(Klaxon().parseArray(result.get()))
            }
        }
    }

    private fun eventRequest(honeyUri: String, event: Event): Request {
        return honeyUri.httpPost()
                .header(HEADER_HONEYCOMB_TEAM to event.writeKey,
                        HEADER_HONEYCOMB_EVENT_TIME to event.timeStamp,
                        HEADER_HONEYCOMB_SAMPLE_RATE to event.sampleRate)
                .body(toJsonString(event))
    }

    private fun eventRequest(honeyUri: String, honeyConfig: HoneyConfig, events: List<Event>): Request {
        return honeyUri.httpPost()
                .header(HEADER_HONEYCOMB_TEAM to honeyConfig.writeKey)
                .body(toJsonString(events))
    }


    private fun markerRequest(marker: Marker? = null, method: Method, honeyConfig: HoneyConfig): Request {
        val honeyUri: String
        when (method) {
            POST -> {
                honeyUri = "${honeyConfig.apiHost}$MARKERS_PATH${honeyConfig.dataSet}"
            }
            PUT -> {
                honeyUri = "${honeyConfig.apiHost}$MARKERS_PATH${honeyConfig.dataSet}/${marker?.id}"
            }
            DELETE -> {
                honeyUri = "${honeyConfig.apiHost}$MARKERS_PATH${honeyConfig.dataSet}/${marker?.id}"
            }
            GET -> {
                honeyUri = "${honeyConfig.apiHost}$MARKERS_PATH${honeyConfig.dataSet}"
            }
            else -> {
                logger.warn { "Method $method not supported" }
                throw IllegalArgumentException()
            }
        }
        val body = marker?.let { toJsonString(it) } ?: ""
        return FuelManager.instance.request(method, honeyUri)
                .header(HEADER_HONEYCOMB_TEAM to honeyConfig.writeKey)
                .body(body)
    }
}
