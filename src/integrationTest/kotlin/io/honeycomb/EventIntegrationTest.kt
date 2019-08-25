package io.honeycomb

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.core.extensions.cUrlString
import io.honeycomb.net.blockingSend
import io.honeycomb.net.send
import io.kotlintest.fail
import io.kotlintest.matchers.string.shouldInclude
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import java.net.HttpURLConnection
import java.time.LocalDateTime

class EventIntegrationTest : FunSpec({

    test("simple event should succeed") {
        val now = LocalDateTime.now()
        val (_, response, _) = Event.newEvent(honeyConfig, now)
            .add("string", "bar")
            .add("integer", 1)
            .add("float", 1.1f)
            .add("bool", true)
            .add("date", now)
            .add("array", listOf(1, 2, 3, 4))
            .add("range", 1..4)
            .blockingSend()
        response.statusCode shouldBe HttpURLConnection.HTTP_OK
    }

    test("events with dynamic values should succeed") {
        for (i in 1..10) {
            GlobalConfig.addField { Pair("heap_free", Runtime.getRuntime().freeMemory()) }
            val event = Event.newEvent(honeyConfig, LocalDateTime.now())
            val (_, response, _) = event.blockingSend()
            response.statusCode shouldBe HttpURLConnection.HTTP_OK
        }
    }

    test("global configs are applied to the event") {
        GlobalConfig.addField("hello", "world")
        val now = LocalDateTime.now()
        val (request, response, _) = Event.newEvent(honeyConfig, now)
            .add("string", "bar")
            .add("integer", 1)
            .add("float", 1.1f)
            .add("bool", true)
            .add("date", now)
            .add("array", listOf(1, 2, 3, 4))
            .add("range", 1..4)
            .blockingSend()
        response.statusCode shouldBe HttpURLConnection.HTTP_OK
        // rather dirty check
        request.cUrlString() shouldInclude ("""\"hello\": \"world\"""")
    }

    test("async sending event should succeed") {
        val now = LocalDateTime.now()
        Event.newEvent(honeyConfig, now)
            .add("string", "bar")
            .add("integer", 1)
            .add("float", 1.1f)
            .add("bool", true)
            .add("date", now)
            .add("array", listOf(1, 2, 3, 4))
            .add("range", 1..4)
            .send { _, response, result ->
                result.fold({
                    response.statusCode shouldBe HttpURLConnection.HTTP_OK
                }, { err ->
                    err.message?.let { fail(it) }
                })
            }
    }

    test("batching tramissions works") {
        val event1 = Event.newEvent(honeyConfig, LocalDateTime.now())
            .add("string", "bar")
            .add("integer", 1)
            .add("float", 1.1f)
            .add("bool", true)
            .add("date", LocalDateTime.now())
            .add("array", listOf(1, 2, 3, 4))
            .add("range", 1..4)
        val event2 = Event.newEvent(honeyConfig, LocalDateTime.now())
            .add("string", "bar")
            .add("integer", 1)
            .add("float", 1.1f)
            .add("bool", true)
            .add("date", LocalDateTime.now())
            .add("array", listOf(1, 2, 3, 4))
            .add("range", 1..4)
        val (_, response, result) = listOf(event1, event2).blockingSend(honeyConfig)
        response.statusCode shouldBe HttpURLConnection.HTTP_OK
        val parser = Parser.default()
        val stringBuilder = StringBuilder(result.get())
        @Suppress("UNCHECKED_CAST")
        val jsonArray = parser.parse(stringBuilder) as JsonArray<JsonObject>
        jsonArray.size shouldBe 2
        for (i in 0..1) {
            jsonArray[i]["status"] shouldBe 202
        }
    }

    test("an invalid key should fail with `unauthorized`") {
        val now = LocalDateTime.now()
        val honeyConfig = HoneyConfig(writeKey = "garbage", dataSet = "kotlintest")
        val (_, response, _) = Event.newEvent(honeyConfig, now).add("string", "bar").blockingSend()
        response.statusCode shouldBe HttpURLConnection.HTTP_UNAUTHORIZED
    }
})
