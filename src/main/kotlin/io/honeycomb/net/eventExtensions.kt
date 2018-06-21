package io.honeycomb.net

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import io.honeycomb.Event
import io.honeycomb.GlobalConfig
import io.honeycomb.HoneyConfig

private const val EVENTS_PATH = "/1/events/"
private const val BATCH_EVENTS_PATH = "/1/batch/"

/**
 * Transmits the [Event] to the API. Merges in any fields found in [GlobalConfig] before transmission
 *
 * This is a _blocking_ request and you will need to handle the result
 *
 * @return the response [Triple]
 */
fun Event.blockingSend(): Triple<Request, Response, Result<String, FuelError>> {
    val eventsWithGlobalPairs = GlobalConfig.applyFields(this)
    return Transmit.eventRequest(
        "${eventsWithGlobalPairs.apiHost}$EVENTS_PATH${eventsWithGlobalPairs.dataSet}",
        eventsWithGlobalPairs
    ).responseString()
}

/**
 * Batch transmits events to the API. Merges in any fields found in [GlobalConfig] before transmission
 *
 * This is a _blocking_ request and you will need to handle the result
 *
 * @return the response [Triple]
 */
fun List<Event>.blockingSend(honeyConfig: HoneyConfig): Triple<Request, Response, Result<String, FuelError>> {
    val eventsWithGlobalPairs = this.map { GlobalConfig.applyFields(it) }
    return Transmit.eventRequest(
        "${honeyConfig.apiHost}$BATCH_EVENTS_PATH${honeyConfig.dataSet}",
        honeyConfig,
        eventsWithGlobalPairs
    ).responseString()
}

/**
 * Transmits the [Event] to the API. Merges in any fields found in [GlobalConfig] before transmission
 *
 * This is an _async_ request.
 *
 */
fun Event.send() {
    Transmit.submit(this) { _, response, result ->
        result.fold({ _ ->
            Transmit.logger.debug { response.statusCode }
        }, { err ->
            Transmit.logger.warn { err }
        })
    }
}

/**
 * Transmits the [Event] to the API. Merges in any fields found in [GlobalConfig] before transmission
 *
 * This is an _async_ request. You need to provide a handler, in order to evaluate the response.
 *
 * @param handler the result handler
 */
fun Event.send(handler: (Request, Response, Result<String, FuelError>) -> Unit) {
    Transmit.submit(this, handler)
}
