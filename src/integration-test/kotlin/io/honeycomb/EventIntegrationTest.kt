package io.honeycomb

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import java.time.LocalDateTime

class EventIntegrationTest {


    private val honeyConfig = HoneyConfig(writeKey = System.getenv("WRITE_KEY"), dataSet = "libhoney-kt-test")

    @Test
    fun checksTransmission() {
        val now = LocalDateTime.now()
        val event = Event.newEvent(honeyConfig, now)
                .add("string", "bar")
                .add("integer", 1)
                .add("float", 1.1f)
                .add("bool", true)
                .add("date", now)
                .add("array", listOf(1, 2, 3, 4))
                .add("range", 1..4)
        val (_, response, _) = Transmit.blockingSend(event)
        assertThat(response.statusCode).isEqualTo(HttpURLConnection.HTTP_OK)
    }

    @Test
    fun checksTransmissionWithGlobalConfig() {
        GlobalConfig.addField("hello","world")
        val now = LocalDateTime.now()
        val event = Event.newEvent(honeyConfig, now)
                .add("string", "bar")
                .add("integer", 1)
                .add("float", 1.1f)
                .add("bool", true)
                .add("date", now)
                .add("array", listOf(1, 2, 3, 4))
                .add("range", 1..4)
        val (request, response, _) = Transmit.blockingSend(event)
        assertThat(response.statusCode).isEqualTo(HttpURLConnection.HTTP_OK)
        //rather dirty check
        assertThat(request.cUrlString().contains("\\\"hello\\\":\\\"world\\\"")).isTrue()
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
        val (_, response, result) = Transmit.blockingSend(listOf(event1, event2), honeyConfig)
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
        val event = Event.newEvent(honeyConfig, now).add("string", "bar")
        val (_, response, _) = Transmit.blockingSend(event)
        assertThat(response.statusCode).isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED)
    }
}
