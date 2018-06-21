package io.honeycomb.collections

import io.honeycomb.Event
import io.honeycomb.HoneyConfig
import io.honeycomb.Tuning
import io.honeycomb.net.send
import java.time.LocalDateTime
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

private const val ELEMENT_TYPE = "element-type"
private const val CORE_POOL_SIZE = 5
private const val KEEP_ALIVE = 30L
private val executorService by lazy {
    val threadPoolExecutor = ThreadPoolExecutor(
        CORE_POOL_SIZE,
        Tuning.threadCount,
        KEEP_ALIVE, TimeUnit.SECONDS,
        LinkedBlockingDeque<Runnable>(Tuning.maxQueueSize)
    )
    threadPoolExecutor.prestartCoreThread()
    Runtime.getRuntime().addShutdownHook(Thread { shutdown() })
    threadPoolExecutor
}

internal inline fun <reified V : Any, T> observe(operation: V, prefix: String, honeyConfig: HoneyConfig, element: T) {
    executorService.submit {
        Event.newEvent(honeyConfig, LocalDateTime.now())
            .add("$prefix.${operation::class.java.enclosingMethod.name}", element as Any)
            .add(ELEMENT_TYPE, element.javaClass.name)
            .send()
    }
}

internal inline fun <reified V : Any, T> observe(
    operation: V,
    prefix: String,
    honeyConfig: HoneyConfig,
    elements: Collection<T>
) {
    executorService.submit {
        Event.newEvent(honeyConfig, LocalDateTime.now())
            .add("$prefix.${operation::class.java.enclosingMethod.name}", elements)
            .add(ELEMENT_TYPE, elements.javaClass.name)
            .send()
    }
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
