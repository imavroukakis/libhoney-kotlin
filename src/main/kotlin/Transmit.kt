import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpPost

object Transmit {

    private const val HONEYCOMB_TEAM_HEADER = "X-Honeycomb-Team"
    private const val HONEYCOMB_EVENT_TIME = "X-Honeycomb-Event-Time"
    private const val HONEYCOMB_SAMPLE_RATE = "X-Honeycomb-Samplerate"
    private val contentType = "Content-Type" to "application/json"
    private val userAgent = "User-Agent" to "libhoney-kt/0.0.1"

    fun send(event: Event): Request {
        val honeyUri = event.apiHost + "/1/events/" + event.dataSet
        return honeyUri.httpPost()
                .header(HONEYCOMB_TEAM_HEADER to event.writeKey,
                        contentType,
                        userAgent,
                        HONEYCOMB_EVENT_TIME to event.timeStamp,
                        HONEYCOMB_SAMPLE_RATE to event.sampleRate)
                .body(event.toJsonString())
                .responseString { _, _, _ -> }
    }
}
