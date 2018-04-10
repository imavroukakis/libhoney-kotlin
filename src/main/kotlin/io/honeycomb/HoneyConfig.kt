package io.honeycomb

/**
 * General configuration that can be applied to an [io.honeycomb.Event]
 * The class will initialise with the following defaults
 * `apiHost = https://api.honeycomb.io`
 * `sampleRate = 1`
 *  a valid [writeKey] and [dataSet] are required
 *
 * @constructor
 */
class HoneyConfig(
    val writeKey: String,
    val dataSet: String,
    val apiHost: String = "https://api.honeycomb.io",
    val sampleRate: Int = 1
)
