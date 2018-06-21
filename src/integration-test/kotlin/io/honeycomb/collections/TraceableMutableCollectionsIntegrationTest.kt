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

class TraceableMutableCollectionsIntegrationTest {

    private val logger = KotlinLogging.logger {}

    @Before
    fun clearGlobalConfig() {
        GlobalConfig.clearAllFields()
    }

    @Test
    fun testTraceableSetInt() {
        Tuning.threadCount = 50
        Tuning.maxQueueSize = 3000
        val set = ObservableMutableSet<Int>(honeyConfig)
        val stopwatchOverall = Stopwatch.createStarted()
        set.addAll(arrayListOf(99, 98, 97))
        val stopwatch = Stopwatch.createStarted()
        for (i in 1..50) {
            set.add(i)
        }
        logger.info { "Submitted all to traced set in ${stopwatch.stop().elapsed().toMillis()} millis" }
        stopwatch.reset().start()
        set.clear()
        Awaitility.await().until {
            logger.info { "${pendingTransmissions()} transmissions remaining " }
            pendingTransmissions() == 0
        }
        logger.info { "took ${stopwatchOverall.stop().elapsed().toMillis()} millis" }
    }

    @Test
    fun testTraceableSetObject() {
        Tuning.threadCount = 50
        Tuning.maxQueueSize = 3000
        val set = ObservableMutableSet<DataObject>(honeyConfig)
        val stopwatchOverall = Stopwatch.createStarted()
        val stopwatch = Stopwatch.createStarted()
        for (i in 1..50) {
            val element = DataObject(i.toString(10), i % 2 == 0, AnotherDataObject(i.toString(10)))
            set.add(element)
        }
        logger.info { "Submitted all to traced set in ${stopwatch.stop().elapsed().toMillis()} millis" }
        stopwatch.reset().start()
        set.clear()
        Awaitility.await().until {
            logger.info { "${pendingTransmissions()} transmissions remaining " }
            pendingTransmissions() == 0
        }
        logger.info { "took ${stopwatchOverall.stop().elapsed().toMillis()} millis" }
    }

    @Test
    fun testTraceableListInt() {
        Tuning.threadCount = 50
        Tuning.maxQueueSize = 3000
        val list = ObservableMutableList<Int>(honeyConfig)
        val stopwatchOverall = Stopwatch.createStarted()
        list.addAll(arrayListOf(99, 98, 97))
        val stopwatch = Stopwatch.createStarted()
        for (i in 1..50) {
            list.add(i)
        }
        logger.info { "Submitted all to traced list in ${stopwatch.stop().elapsed().toMillis()} millis" }
        stopwatch.reset().start()
        list.clear()
        Awaitility.await().until {
            logger.info { "${pendingTransmissions()} transmissions remaining " }
            pendingTransmissions() == 0
        }
        logger.info { "took ${stopwatchOverall.stop().elapsed().toMillis()} millis" }
    }

    @Test
    fun testTraceableLinkedListInt() {
        Tuning.threadCount = 50
        Tuning.maxQueueSize = 3000
        val list = ObservableMutableLinkedList<Int>(honeyConfig)
        val stopwatchOverall = Stopwatch.createStarted()
        list.addAll(arrayListOf(99, 98, 97))
        val stopwatch = Stopwatch.createStarted()
        for (i in 1..50) {
            list.add(i)
        }
        logger.info { "Submitted all to traced linked list in ${stopwatch.stop().elapsed().toMillis()} millis" }
        stopwatch.reset().start()
        list.clear()
        Awaitility.await().until {
            logger.info { "${pendingTransmissions()} transmissions remaining " }
            pendingTransmissions() == 0
        }
        logger.info { "took ${stopwatchOverall.stop().elapsed().toMillis()} millis" }
    }

    @Test
    fun testTraceableMapStringInt() {
        Tuning.threadCount = 50
        Tuning.maxQueueSize = 3000
        val map = ObservableMutableMap<String, Int>(honeyConfig)
        val stopwatchOverall = Stopwatch.createStarted()
        val stopwatch = Stopwatch.createStarted()
        for (i in 1..100) {
            map[i.toString(10)] = i
        }
        logger.info { "Submitted all to traced map in ${stopwatch.stop().elapsed().toMillis()} millis" }
        stopwatch.reset().start()
        map.clear()
        Awaitility.await().until {
            logger.info { "${pendingTransmissions()} transmissions remaining " }
            pendingTransmissions() == 0
        }
        logger.info { "took ${stopwatchOverall.stop().elapsed().toMillis()} millis" }
    }
}
