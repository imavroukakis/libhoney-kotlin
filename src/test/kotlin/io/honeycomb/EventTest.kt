package io.honeycomb

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime

class EventTest {

    private val writeKey: String = "WRITE_KEY"
    private val dataSet: String = "data_set"
    private val honeyConfig: HoneyConfig = HoneyConfig(writeKey, dataSet)

    @Test
    fun checksDefaults() {
        val now = LocalDateTime.now()
        val event = Event.newEvent(honeyConfig)
        assertThat(event.timeStamp).isNotNull()
        assertThat(event.timeStamp).isAfterOrEqualTo(now)
        assertThat(event.sampleRate).isEqualTo(1)
    }

    @Test
    fun checksTimeAssignment() {
        val now = LocalDateTime.now()
        val event = Event.newEvent(honeyConfig, now)
        assertThat(event.timeStamp).isNotNull()
        assertThat(event.timeStamp).isEqualTo(now)
    }

    @Test
    fun eventAdd() {
        val now = LocalDateTime.now()
        val event = Event.newEvent(honeyConfig, now).add("string", "bar")
                .add("integer", 1)
                .add("float", 1.1f)
                .add("bool", true)
                .add("date", now)
                .add("array", listOf(1, 2, 3, 4))
                .add("range", 1..4)
        assertThat(toJson(event)).isNotEmpty
        assertThat(toJson(event)).satisfies { t ->
            assertThat(t["string"]).isEqualTo("bar")
            assertThat(t["integer"]).isEqualTo(1)
            assertThat(t["float"]).isEqualTo(1.1f)
            assertThat(t["bool"]).isEqualTo(true)
            assertThat(t["date"]).isEqualTo(toRfc3339(now))
            assertThat(t["array"]).isEqualTo(listOf(1, 2, 3, 4))
            assertThat(t["range"]).isEqualTo(listOf(1, 2, 3, 4))
        }
    }
}
