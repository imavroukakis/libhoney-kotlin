package io.honeycomb
/**
 * General configuration that can be applied to an {@code io.honeycomb.Event}
 */
class HoneyConfig(
        val writeKey: String = "",
        val dataSet: String = "",
        val apiHost: String = "https://api.honeycomb.io",
        val sampleRate: Int = 1)
