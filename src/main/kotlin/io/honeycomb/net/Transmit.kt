package io.honeycomb.net

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Method.DELETE
import com.github.kittinunf.fuel.core.Method.GET
import com.github.kittinunf.fuel.core.Method.POST
import com.github.kittinunf.fuel.core.Method.PUT
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.extensions.cUrlString
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import io.honeycomb.Event
import io.honeycomb.HoneyConfig
import io.honeycomb.Marker
import io.honeycomb.Tuning
import io.honeycomb.toJsonString
import io.honeycomb.toRfc3339
import mu.KotlinLogging
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Get the number of [Event] and [Marker] waiting to be submitted. The number is made up of both the in-flight and queued
 * objects.
 * @return the number of [Event] and [Marker] waiting to be submitted.
 */
fun pendingTransmissions() = Transmit.remainingTransmissions()

internal object Transmit {

    private const val HEADER_HONEYCOMB_TEAM = "X-Honeycomb-Team"
    private const val HEADER_HONEYCOMB_EVENT_TIME = "X-Honeycomb-io.honeycomb.Event-Time"
    private const val HEADER_HONEYCOMB_SAMPLE_RATE = "X-Honeycomb-Samplerate"
    private const val MARKERS_PATH = "/1/markers/"
    private const val SHUTDOWN_MILLIS = 2000L
    private const val CORE_POOL_SIZE = 5
    private const val KEEP_ALIVE_SECONDS = 30L
    private val executorService by lazy {
        val policy: RejectedExecutionHandler = if (Tuning.retryPolicy == Tuning.RetryPolicy.RETRY) {
            LoggingCallerRunsPolicy()
        } else {
            LoggingDiscardPolicy()
        }

        val threadPoolExecutor = ThreadPoolExecutor(
            CORE_POOL_SIZE,
            Tuning.threadCount,
            KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
            LinkedBlockingDeque<Runnable>(Tuning.maxQueueSize),
            DaemonThreadFactory(),
            policy
        )
        threadPoolExecutor.prestartCoreThread()
        threadPoolExecutor
    }
    val logger = KotlinLogging.logger {}

    init {
        FuelManager.instance.baseHeaders = mapOf(
            "Content-Type" to "application/json",
            "User-Agent" to "libhoney-kt/0.2.0"
        )
        Runtime.getRuntime().addShutdownHook(Thread(Transmit::shutdown))
    }

    fun remainingTransmissions() = executorService.activeCount + executorService.queue.size

    fun eventRequest(honeyUri: String, event: Event): Request {
        logger.debug { event.toJsonString() }
        return honeyUri.httpPost()
            .header(
                HEADER_HONEYCOMB_TEAM to event.writeKey,
                HEADER_HONEYCOMB_EVENT_TIME to event.timeStamp.toRfc3339(),
                HEADER_HONEYCOMB_SAMPLE_RATE to event.sampleRate
            )
            .body(event.toJsonString())
    }

    fun eventRequest(honeyUri: String, honeyConfig: HoneyConfig, events: List<Event>): Request {
        logger.debug { events.toJsonString() }
        return honeyUri.httpPost()
            .header(HEADER_HONEYCOMB_TEAM to honeyConfig.writeKey)
            .body(events.toJsonString())
    }

    fun postMarker(marker: Marker, honeyConfig: HoneyConfig): Request {
        val honeyUri = "${honeyConfig.apiHost}$MARKERS_PATH${honeyConfig.dataSet}"
        return markerRequest(marker, POST, honeyUri, honeyConfig.writeKey)
    }

    fun putMarker(marker: Marker, honeyConfig: HoneyConfig): Request {
        val honeyUri = "${honeyConfig.apiHost}$MARKERS_PATH${honeyConfig.dataSet}/${marker.id}"
        return markerRequest(marker, PUT, honeyUri, honeyConfig.writeKey)
    }

    fun deleteMarker(marker: Marker, honeyConfig: HoneyConfig): Request {
        val honeyUri = "${honeyConfig.apiHost}$MARKERS_PATH${honeyConfig.dataSet}/${marker.id}"
        return markerRequest(marker, DELETE, honeyUri, honeyConfig.writeKey)
    }

    fun getMarkers(honeyConfig: HoneyConfig): Request {
        val honeyUri = "${honeyConfig.apiHost}$MARKERS_PATH${honeyConfig.dataSet}"
        return markerRequest(honeyUri, honeyConfig.writeKey)
    }

    private fun markerRequest(honeyUri: String, writeKey: String): Request {
        return FuelManager.instance.request(GET, honeyUri)
            .header(HEADER_HONEYCOMB_TEAM to writeKey)
            .body("")
    }

    fun markerRequest(marker: Marker, method: Method, honeyUri: String, writeKey: String): Request {
        when (method) {
            PUT, POST, DELETE -> {
                return FuelManager.instance.request(method, honeyUri)
                    .header(HEADER_HONEYCOMB_TEAM to writeKey)
                    .body(marker.toJsonString())
            }
            else -> {
                logger.warn { "Method $method not supported" }
                throw IllegalArgumentException()
            }
        }
    }

    fun submit(event: Event, handler: (Request, Response, Result<String, FuelError>) -> Unit) {
        executorService.submit {
            val (request, response, result) = event.blockingSend()
            logger.debug { "invoking handler on ${Thread.currentThread().name} for ${request.cUrlString()}" }
            handler(request, response, result)
        }
    }

    private fun shutdown() {
        executorService.shutdown()
        try {
            if (!executorService.awaitTermination(SHUTDOWN_MILLIS, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executorService.shutdownNow()
        }
    }

    private class LoggingCallerRunsPolicy(
        val innerPolicy: RejectedExecutionHandler = ThreadPoolExecutor.CallerRunsPolicy()
    ) : RejectedExecutionHandler by innerPolicy {

        override fun rejectedExecution(r: Runnable, executor: ThreadPoolExecutor) {
            innerPolicy.rejectedExecution(r, executor)
            logger.debug { "rejected execution $r|queued:${executor.queue.size}|active:${executor.activeCount}" }
        }
    }

    private class LoggingDiscardPolicy(
        val innerPolicy: RejectedExecutionHandler = ThreadPoolExecutor.DiscardPolicy()
    ) : RejectedExecutionHandler by innerPolicy {

        override fun rejectedExecution(r: Runnable, executor: ThreadPoolExecutor) {
            innerPolicy.rejectedExecution(r, executor)
            logger.debug { "rejected execution $r|queued:${executor.queue.size}|active:${executor.activeCount}" }
        }
    }

    private class DaemonThreadFactory : ThreadFactory {
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
