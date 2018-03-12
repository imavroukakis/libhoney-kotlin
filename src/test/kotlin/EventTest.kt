import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EventTest {


    @Test
    fun checksDefaults() {
        val now = LocalDateTime.now()
        val event = Event(HoneyConfig())
        assertThat(event.timeStamp).isNotNull()
        assertThat(event.timeStamp).isAfter(now)
    }

    @Test
    fun checksTimeAssignment() {
        val now = LocalDateTime.now()
        val event = Event(HoneyConfig(), now)
        assertThat(event.timeStamp).isNotNull()
        assertThat(event.timeStamp).isEqualTo(now)
    }

    @Test
    fun eventAdd() {
        val now = LocalDateTime.now()
        val event = Event(HoneyConfig(), now).add("string", "bar")
                .add("integer", 1)
                .add("float", 1.1f)
                .add("bool", true)
                .add("date", now)
                .add("array", listOf(1, 2, 3, 4))
                .add("range", 1..4)
        assertThat(event.toJson()).isNotEmpty
        println(event.toJsonString())
        assertThat(event.toJson()).satisfies { t ->
            assertThat(t["string"]).isEqualTo("bar")
            assertThat(t["integer"]).isEqualTo(1)
            assertThat(t["float"]).isEqualTo(1.1f)
            assertThat(t["bool"]).isEqualTo(true)
            assertThat(t["date"]).isEqualTo(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(now))
            assertThat(t["array"]).isEqualTo(listOf(1, 2, 3, 4))
            assertThat(t["range"]).isEqualTo(listOf(1, 2, 3, 4))
        }

    }
}
