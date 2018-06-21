package io.honeycomb

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.Fuel
import io.honeycomb.net.blockingSend
import io.honeycomb.net.send
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Fail
import org.junit.Test
import java.net.HttpURLConnection
import java.time.LocalDateTime

class EventIntegrationTest {

    @Test
    fun checksTransmission() {
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
        assertThat(response.statusCode).isEqualTo(HttpURLConnection.HTTP_OK)
    }

    @Test
    fun checksTransmissionWithDynamicFields() {
        for (i in 1..10) {
            GlobalConfig.addField { Pair("heap_free", Runtime.getRuntime().freeMemory()) }
            val event = Event.newEvent(honeyConfig, LocalDateTime.now())
            val (_, response, _) = event.blockingSend()
            assertThat(response.statusCode).isEqualTo(HttpURLConnection.HTTP_OK)
        }
    }

    @Test
    fun checksTransmissionWithGlobalConfig() {
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
        assertThat(response.statusCode).isEqualTo(HttpURLConnection.HTTP_OK)
        // rather dirty check
        assertThat(request.cUrlString()).contains("hello", "world")
    }

    @Test
    fun checksAsyncTransmission() {
        Fuel.testMode()
        val now = LocalDateTime.now()
        Event.newEvent(honeyConfig, now)
                .add("string", "bar")
                .add("integer", 1)
                .add("float", 1.1f)
                .add("bool", true)
                .add("date", now)
                .add("array", listOf(1, 2, 3, 4))
                .add("range", 1..4)
                .send({ _, response, result ->
                    result.fold({ _ ->
                        assertThat(response.statusCode).isEqualTo(HttpURLConnection.HTTP_OK)
                    }, { err ->
                        Fail.fail(err.message)
                    })
                })
    }

    @Test
    fun checksBatchedTransmission() {
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
        assertThat(response.statusCode).isEqualTo(HttpURLConnection.HTTP_OK)
        val parser = Parser()
        val stringBuilder = StringBuilder(result.get())
        @Suppress("UNCHECKED_CAST")
        val jsonArray = parser.parse(stringBuilder) as JsonArray<JsonObject>
        assertThat(jsonArray.size).isEqualTo(2)
        for (i in 0..1) {
            assertThat(jsonArray[i]["status"]).isEqualTo(202)
        }
    }

    @Test
    fun checksFailedTransmissionDueToInvalidWriteKey() {
        val now = LocalDateTime.now()
        val honeyConfig = HoneyConfig(writeKey = "garbage", dataSet = "kotlintest")
        val (_, response, _) = Event.newEvent(honeyConfig, now).add("string", "bar").blockingSend()
        assertThat(response.statusCode).isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED)
    }
}
