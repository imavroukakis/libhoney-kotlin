package io.honeycomb

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class MarkerTest {

    @Test
    fun checkMarkerStructure() {
        val json = toJson(Marker(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                message = "marker created",
                type = "test",
                url = "http://foo"))
        assertThat(json.long("start_time")).isNotNull()
        assertThat(json.long("end_time")).isNull()
        assertThat(json.string("message")).isEqualTo("marker created")
        assertThat(json.string("type")).isEqualTo("test")
        assertThat(json.string("url")).isEqualTo("http://foo")
    }

    @Test
    fun checkMarkerDefaults() {
        val marker = Marker()
        assertThat(marker.startTime).isEqualTo(-1L)
        assertThat(marker.endTime).isEqualTo(-1L)
        assertThat(marker.message).isBlank()
        assertThat(marker.type).isBlank()
        assertThat(marker.url).isBlank()
        assertThat(marker.createdAt).isBlank()
        assertThat(marker.updatedAt).isBlank()
    }
}
