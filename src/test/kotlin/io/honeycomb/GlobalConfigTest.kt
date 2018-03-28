package io.honeycomb

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage
import org.junit.Test
import java.time.LocalDateTime

class GlobalConfigTest {

    private val writeKey: String = "WRITE_KEY"
    private val dataSet: String = "data_set"
    private val honeyConfig: HoneyConfig = HoneyConfig(writeKey, dataSet)

    @Test
    fun dynamicFields() {
        val totalMemory = Runtime.getRuntime().totalMemory()
        val freeMemory = Runtime.getRuntime().freeMemory()
        GlobalConfig.addField { Pair("heap_total", Runtime.getRuntime().totalMemory()) }
        GlobalConfig.addField { Pair("heap_free", Runtime.getRuntime().freeMemory()) }
        GlobalConfig.addField { Pair("time", LocalDateTime.now()) }

        val now = LocalDateTime.now()
        Thread.sleep(100)
        val event = GlobalConfig.applyFields(Event.newEvent(honeyConfig))
        // assert that the higher order function is not applied when added
        val dynamicDate = LocalDateTime.parse(event.data["time"] as String, dateFormat)
        assertThat(event.data["heap_total"] as Long).isPositive().isCloseTo(totalMemory, Percentage.withPercentage(5.0))
        assertThat(event.data["heap_free"] as Long).isPositive().isCloseTo(freeMemory, Percentage.withPercentage(5.0))
        assertThat(dynamicDate).isAfter(now)
    }

    @Test
    fun staticFields() {
        GlobalConfig.addField("num", 1.0f)
        val event = GlobalConfig.applyFields(Event.newEvent(honeyConfig))
        assertThat(event.data["num"] as Float).isEqualTo(1.0f)
    }
}
