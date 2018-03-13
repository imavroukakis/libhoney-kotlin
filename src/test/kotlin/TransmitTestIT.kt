import com.github.kittinunf.fuel.Fuel
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import java.time.LocalDateTime

class TransmitTestIT {


    @Test
    fun checksTransmission() {
        Fuel.testMode()
        val now = LocalDateTime.now()
        val event = Event(HoneyConfig(writeKey = "2f7b1e73ff46ea90a3d9937dd9715435", dataSet = "kotlintest"), now)
                .add("string", "bar")
                .add("integer", 1)
                .add("float", 1.1f)
                .add("bool", true)
                .add("date", now)
                .add("array", listOf(1, 2, 3, 4))
                .add("range", 1..4)
        Transmit.send(event).responseString { _, response, _ ->
            assertThat(response.statusCode).isEqualTo(HttpURLConnection.HTTP_OK)
        }
    }
}
