package io.honeycomb

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.core.Method.*
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import mu.KotlinLogging

object Transmit {

    private val logger = KotlinLogging.logger {}
    private const val HONEYCOMB_TEAM_HEADER = "X-Honeycomb-Team"
    private const val HONEYCOMB_EVENT_TIME = "X-Honeycomb-io.honeycomb.Event-Time"
    private const val HONEYCOMB_SAMPLE_RATE = "X-Honeycomb-Samplerate"
    private const val EVENTS_PATH = "/1/events/"
    private const val MARKERS_PATH = "/1/markers/"
    private val contentType = "Content-Type" to "application/json"
    private val userAgent = "User-Agent" to "libhoney-kt/0.0.2"

    /**
     * Transmits an [Event] to the API. Optionally merges in [GlobalConfig.dataPairs] before transmission
     *
     * This is a _blocking_ request and you will need to handle the result
     *
     * @param event the [Event] to transmit
     * @return the response [Triple]
     */
    fun blockingSend(event: Event): Triple<Request, Response, Result<String, FuelError>> {
        val evt = event.add(GlobalConfig.dataPairs)
        eventRequest(evt.apiHost + EVENTS_PATH + evt.dataSet, evt).responseString()
        return eventRequest(evt.apiHost + EVENTS_PATH + evt.dataSet, evt).responseString()
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
     * Transmits an [Event] to the API. Optionally merges in [GlobalConfig.dataPairs] before transmission
     *
     * This is an _async_ request
     *
     * @param event the [Event] to transmit
     */
    fun send(event: Event) {
        val evt = event.add(GlobalConfig.dataPairs)
        eventRequest(evt.apiHost + EVENTS_PATH + evt.dataSet, evt)
                .responseString { _, response, result ->
                    result.fold({ _ ->
                        logger.debug { response.statusCode }
                    }, { err ->
                        logger.warn { err }
                    })
                }
    }

    private fun eventRequest(honeyUri: String, event: Event): Request {
        return honeyUri.httpPost()
                .header(HONEYCOMB_TEAM_HEADER to event.writeKey,
                        contentType,
                        userAgent,
                        HONEYCOMB_EVENT_TIME to event.timeStamp,
                        HONEYCOMB_SAMPLE_RATE to event.sampleRate)
                .body(toJsonString(event))
    }

    private fun markerRequest(marker: Marker, method: Method, honeyConfig: HoneyConfig): Request {
        val body: String
        val honeyUri: String
        when (method) {
            POST -> {
                honeyUri = honeyConfig.apiHost + MARKERS_PATH + honeyConfig.dataSet
                body = toJsonString(marker)
            }
            PUT -> {
                honeyUri = honeyConfig.apiHost + MARKERS_PATH + honeyConfig.dataSet + "/" + marker.id
                body = toJsonString(marker)
            }
            DELETE -> {
                honeyUri = honeyConfig.apiHost + MARKERS_PATH + honeyConfig.dataSet + "/" + marker.id
                body = ""
            }
            else -> {
                logger.warn { "Method $method not supported" }
                throw IllegalArgumentException()
            }
        }
        return FuelManager.instance.request(method, honeyUri)
                .header(HONEYCOMB_TEAM_HEADER to honeyConfig.writeKey,
                        contentType,
                        userAgent)
                .body(body)
    }
}
