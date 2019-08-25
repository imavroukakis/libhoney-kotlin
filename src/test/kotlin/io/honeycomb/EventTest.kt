package io.honeycomb

import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.date.shouldBeAfter
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.FunSpec
import java.time.LocalDateTime

class EventTest : FunSpec({

    val writeKey = "WRITE_KEY"
    val dataSet = "data_set"
    val honeyConfig = HoneyConfig(writeKey, dataSet)

    test("defaults and assignment are sane") {
        val now = LocalDateTime.now()
        val event = Event.newEvent(honeyConfig)
        event.writeKey shouldBe writeKey
        event.dataSet shouldBe dataSet
        event.apiHost shouldBe "https://api.honeycomb.io"
        event.timeStamp shouldNotBe null
        event.timeStamp shouldBeAfter now
        event.sampleRate shouldBe 1

        val eventTwo = Event.newEvent(writeKey = writeKey, dataSet = dataSet, timeStamp = now)
        eventTwo.writeKey shouldBe writeKey
        eventTwo.dataSet shouldBe dataSet
        eventTwo.apiHost shouldBe "https://api.honeycomb.io"
        eventTwo.timeStamp shouldNotBe null
        eventTwo.timeStamp shouldBeSameInstanceAs now
        eventTwo.sampleRate shouldBe 1
    }

    test("time is assigned correctly") {
        val now = LocalDateTime.now()
        val event = Event.newEvent(honeyConfig, now)
        event.timeStamp shouldNotBe null
        event.timeStamp shouldBe now
    }

    test("adding an event") {
        val now = LocalDateTime.now()
        val event = Event.newEvent(honeyConfig, now).add("string", "bar")
            .add("integer", 1)
            .add("float", 1.1f)
            .add("bool", true)
            .add("date", now)
            .add("array", listOf(1, 2, 3, 4))
            .add("range", 1..4)
            .add("long_range", 1L..4L)

        val toJson = event.toJson()
        toJson["string"] shouldBe "bar"
        toJson["integer"] shouldBe 1
        toJson["float"] shouldBe 1.1f
        toJson["bool"] shouldBe true
        toJson["date"] shouldBe now.toRfc3339()
        toJson["array"] as List<*> shouldContainExactly listOf(1, 2, 3, 4)
        toJson["range"] as List<*> shouldContainExactly listOf(1, 2, 3, 4)
        toJson["long_range"] as List<*> shouldContainExactly listOf(1L, 2L, 3L, 4L)
    }
})
