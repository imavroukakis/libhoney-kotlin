package io.honeycomb

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class MarkerTest {

    @Test
    fun checkMarkerStructure() {
        var json = toJson(Marker(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                message = "marker created",
                type = "test",
                url = "http://foo"))
        assertThat(json.long("start_time")).isNotNull()
        assertThat(json.long("end_time")).isNull()
        assertThat(json.string("message")).isEqualTo("marker created")
        assertThat(json.string("type")).isEqualTo("test")
        assertThat(json.string("url")).isEqualTo("http://foo")
    }
}
