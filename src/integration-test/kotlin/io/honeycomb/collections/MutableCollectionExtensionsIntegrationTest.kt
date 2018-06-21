package io.honeycomb.collections

import com.google.common.base.Stopwatch
import io.honeycomb.GlobalConfig
import io.honeycomb.Tuning
import io.honeycomb.honeyConfig
import io.honeycomb.net.pendingTransmissions
import mu.KotlinLogging
import org.awaitility.Awaitility
import org.junit.Before
import org.junit.Test

class MutableCollectionExtensionsIntegrationTest {

    private val logger = KotlinLogging.logger {}

    @Before
    fun clearGlobalConfig() {
        GlobalConfig.clearAllFields()
    }

    @Test
    fun testMutableCollectionExtensions() {
        Tuning.threadCount = 50
        Tuning.maxQueueSize = 3000
        val list: MutableCollection<Int> = ArrayList()
        val stopwatch = Stopwatch.createStarted()
        for (i in 1..200) {
            list.add(i, honeyConfig)
        }
        val elements = listOf(-1, -2, -3)
        list.addAll(elements, honeyConfig)
        list.remove(1, honeyConfig)
        list.removeAll(elements, honeyConfig)
        list.clear(honeyConfig)
        logger.info { "Submitted all operations to traced mutable collection in ${stopwatch.stop().elapsed().toMillis()} millis" }
        stopwatch.reset().start()
        Awaitility.await().until {
            logger.info { "${pendingTransmissions()} transmissions remaining " }
            pendingTransmissions() == 0
        }
    }

    @Test
    fun testMutableMapExtensions() {
        Tuning.threadCount = 50
        Tuning.maxQueueSize = 3000
        val map: MutableMap<String, Int> = HashMap()
        val stopwatch = Stopwatch.createStarted()
        for (i in 1..100) {
            map[i.toString(10), honeyConfig] = i
        }
        map.putAll(mapOf("99" to 99), honeyConfig)
        map.remove("99", honeyConfig)
        map.clear(honeyConfig)
        logger.info { "Submitted all operations to traced mutable collection in ${stopwatch.stop().elapsed().toMillis()} millis" }
        stopwatch.reset().start()
        Awaitility.await().until {
            logger.info { "${pendingTransmissions()} transmissions remaining " }
            pendingTransmissions() == 0
        }
    }
}
