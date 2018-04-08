package io.honeycomb.net

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import io.honeycomb.*
import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Transmits an [Event] to the API. Optionally merges in [GlobalConfig] before transmission
 *
 * This is a _blocking_ request and you will need to handle the result
 *
 * @param event the [Event] to transmit
 * @return the response [Triple]
 */
fun Event.blockingSend(): Triple<Request, Response, Result<String, FuelError>> {
    val eventsWithGlobalPairs = GlobalConfig.applyFields(this)
    return Transmit.eventRequest(
            "${eventsWithGlobalPairs.apiHost}${Transmit.EVENTS_PATH}${eventsWithGlobalPairs.dataSet}",
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
fun List<Event>.blockingSend(honeyConfig: HoneyConfig): Triple<Request, Response, Result<String, FuelError>> {
    val eventsWithGlobalPairs = this.map { GlobalConfig.applyFields(it) }
    return Transmit.eventRequest(
            "${honeyConfig.apiHost}${Transmit.BATCH_EVENTS_PATH}${honeyConfig.dataSet}",
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
fun Event.send(handler: ((Request, Response, Result<String, FuelError>) -> Unit)? = null) {
    val event = GlobalConfig.applyFields(this)
    val safeHandler: (Request, Response, Result<String, FuelError>) -> Unit = handler ?: { _, response, result ->
        result.fold({ _ ->
            Transmit.logger.debug { response.statusCode }
        }, { err ->
            Transmit.logger.warn { err }
        })
    }
    Transmit.submit(event, safeHandler)
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
fun Marker.create(honeyConfig: HoneyConfig): Result<Marker, Exception> {
    val (_, _, result) = Transmit.markerRequest(this, Method.POST, honeyConfig).responseString()
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
fun Marker.update(honeyConfig: HoneyConfig): Result<Marker, Exception> {
    val (_, _, result) = Transmit.markerRequest(this, Method.PUT, honeyConfig).responseString()
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
fun Marker.remove(honeyConfig: HoneyConfig): Result<Marker, Exception> {
    val (_, _, result) = Transmit.markerRequest(this, Method.DELETE, honeyConfig).responseString()
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
    val (_, _, result) = Transmit.markerRequest(honeyConfig = honeyConfig, method = Method.GET).responseString()
    return when (result) {
        is Result.Failure -> {
            Result.error(result.error)
        }
        is Result.Success -> {
            Result.of(Klaxon().parseArray(result.get()))
        }
    }
}

internal object Transmit {

    private const val HEADER_HONEYCOMB_TEAM = "X-Honeycomb-Team"
    private const val HEADER_HONEYCOMB_EVENT_TIME = "X-Honeycomb-io.honeycomb.Event-Time"
    private const val HEADER_HONEYCOMB_SAMPLE_RATE = "X-Honeycomb-Samplerate"
    private const val MARKERS_PATH = "/1/markers/"
    internal const val EVENTS_PATH = "/1/events/"
    internal const val BATCH_EVENTS_PATH = "/1/batch/"
    private val executorService = Executors.newFixedThreadPool(20, DaemonThreadFactory())
    internal val logger = KotlinLogging.logger {}

    init {
        FuelManager.instance.baseHeaders = mapOf(
                "Content-Type" to "application/json",
                "User-Agent" to "libhoney-kt/0.1.0"
        )
        Runtime.getRuntime().addShutdownHook(Thread(Transmit::shutdown))
    }


    private fun shutdown() {
        executorService.shutdown()
        try {
            if (!executorService.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executorService.shutdownNow()
        }
    }

    internal fun eventRequest(honeyUri: String, event: Event): Request {
        return honeyUri.httpPost()
                .header(HEADER_HONEYCOMB_TEAM to event.writeKey,
                        HEADER_HONEYCOMB_EVENT_TIME to event.timeStamp.toRfc3339(),
                        HEADER_HONEYCOMB_SAMPLE_RATE to event.sampleRate)
                .body(event.toJsonString())
    }

    internal fun eventRequest(honeyUri: String, honeyConfig: HoneyConfig, events: List<Event>): Request {
        return honeyUri.httpPost()
                .header(HEADER_HONEYCOMB_TEAM to honeyConfig.writeKey)
                .body(events.toJsonString())
    }


    internal fun markerRequest(marker: Marker? = null, method: Method, honeyConfig: HoneyConfig): Request {
        val honeyUri: String
        when (method) {
            Method.POST -> {
                honeyUri = "${honeyConfig.apiHost}$MARKERS_PATH${honeyConfig.dataSet}"
            }
            Method.PUT -> {
                honeyUri = "${honeyConfig.apiHost}$MARKERS_PATH${honeyConfig.dataSet}/${marker?.id}"
            }
            Method.DELETE -> {
                honeyUri = "${honeyConfig.apiHost}$MARKERS_PATH${honeyConfig.dataSet}/${marker?.id}"
            }
            Method.GET -> {
                honeyUri = "${honeyConfig.apiHost}$MARKERS_PATH${honeyConfig.dataSet}"
            }
            else -> {
                logger.warn { "Method $method not supported" }
                throw IllegalArgumentException()
            }
        }
        val body = marker?.toJsonString() ?: ""
        return FuelManager.instance.request(method, honeyUri)
                .header(HEADER_HONEYCOMB_TEAM to honeyConfig.writeKey)
                .body(body)
    }

    internal fun submit(event: Event, safeHandler: (Request, Response, Result<String, FuelError>) -> Unit) {
        executorService.submit({
            val (request, response, result) = event.blockingSend()
            Transmit.logger.debug { "invoking handler on ${Thread.currentThread().name}" }
            safeHandler.invoke(request, response, result)
        })
    }

    internal class DaemonThreadFactory : ThreadFactory {
        private val poolNumber = AtomicInteger(1)
        private val group: ThreadGroup
        private val threadNumber = AtomicInteger(1)
        private val namePrefix: String

        init {
            val s = System.getSecurityManager()
            group = s?.threadGroup ?: Thread.currentThread().threadGroup
            namePrefix = "transmit-${poolNumber.getAndIncrement()}-thread-"
        }

        override fun newThread(r: Runnable): Thread {
            val t = Thread(group, r, "$namePrefix${threadNumber.getAndIncrement()}", 0)
            t.isDaemon = true
            t.priority = Thread.NORM_PRIORITY
            return t
        }
    }
}
