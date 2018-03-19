package io.honeycomb

import com.github.kittinunf.result.Result
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class MarkerIntegrationTest {

    private val logger = KotlinLogging.logger {}
    private val honeyConfig = HoneyConfig(writeKey = System.getenv("WRITE_KEY"), dataSet = "libhoney-kt-test")

    @Test
    fun markerLifeCycle() {
        val marker = Marker(LocalDateTime.now().minusHours(1).toEpochSecond(ZoneOffset.UTC),
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                message = "marker created",
                type = "integration test",
                url = "http://foo")
        val result = Transmit.createMarker(marker, honeyConfig)
        assertThat(result).isInstanceOf(Result.Success::class.java)
        logger.info { "Created marker with id ${result.get().id}" }

        val modifiedStartTime = LocalDateTime.now().minusHours(5).toEpochSecond(ZoneOffset.UTC)
        val modifiedMarkerResult = Transmit.updateMarker(
                result.get().copy(startTime = modifiedStartTime, message = "marker updated"), honeyConfig)
        assertThat(modifiedMarkerResult).isInstanceOf(Result.Success::class.java)
        assertThat(modifiedMarkerResult.get().endTime).isEqualTo(marker.endTime)
        assertThat(modifiedMarkerResult.get().message).isEqualTo("marker updated")
        assertThat(modifiedMarkerResult.get().startTime).isEqualTo(modifiedStartTime)
        logger.info { "Updated marker with id ${modifiedMarkerResult.get().id}" }

        val removedMarkerResult = Transmit.removeMarker(modifiedMarkerResult.get(), honeyConfig)
        assertThat(removedMarkerResult).isInstanceOf(Result.Success::class.java)
        logger.info { "Removed marker with id ${removedMarkerResult.get().id}" }
    }
}