package io.honeycomb

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import mu.KotlinLogging

object Transmit {

    private val logger = KotlinLogging.logger {}
    private const val HONEYCOMB_TEAM_HEADER = "X-Honeycomb-Team"
    private const val HONEYCOMB_EVENT_TIME = "X-Honeycomb-io.honeycomb.Event-Time"
    private const val HONEYCOMB_SAMPLE_RATE = "X-Honeycomb-Samplerate"
    private const val PATH = "/1/events/"
    private val contentType = "Content-Type" to "application/json"
    private val userAgent = "User-Agent" to "libhoney-kt/0.0.1"

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
        return createRequest(evt.apiHost + PATH + evt.dataSet, evt).responseString()
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
        createRequest(evt.apiHost + PATH + evt.dataSet, evt)
                .responseString { _, response, result ->
                    result.fold({ data ->
                        logger.debug { response.statusCode }
                    }, { err ->
                        logger.warn { err }
                    })
                }
    }

    private fun createRequest(honeyUri: String, event: Event): Request {
        return honeyUri.httpPost()
                .header(HONEYCOMB_TEAM_HEADER to event.writeKey,
                        contentType,
                        userAgent,
                        HONEYCOMB_EVENT_TIME to event.timeStamp,
                        HONEYCOMB_SAMPLE_RATE to event.sampleRate)
                .body(toJsonString(event))
    }
}
