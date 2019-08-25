package io.honeycomb

import io.kotlintest.matchers.date.shouldBeAfter
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.FunSpec
import java.time.LocalDateTime

class GlobalConfigTest : FunSpec({

    val writeKey = "WRITE_KEY"
    val dataSet = "data_set"
    val honeyConfig = HoneyConfig(writeKey, dataSet)
    val freeMemory = Runtime.getRuntime().freeMemory()

    test("adding dynamic fields") {
        for (i in 1..100) {
            GlobalConfig.clearAllFields()
            GlobalConfig.addField { Pair("heap_free", Runtime.getRuntime().freeMemory()) }
            GlobalConfig.addField { Pair("time", LocalDateTime.now()) }
            val now = LocalDateTime.now()
            Thread.sleep(100)
            val event = GlobalConfig.applyFields(Event.newEvent(honeyConfig))
            // assert that the higher order function is not applied when added
            val dynamicDate = LocalDateTime.parse(event.data["time"] as String, dateFormat)
            event.data["heap_free"] as Long shouldNotBe freeMemory
            dynamicDate shouldBeAfter now
        }
    }

    test("adding static fields") {
        GlobalConfig.addField("num", 1.0f)
        val event = GlobalConfig.applyFields(Event.newEvent(honeyConfig))
        event.data["num"] as Float shouldBe 1.0f
    }
})
