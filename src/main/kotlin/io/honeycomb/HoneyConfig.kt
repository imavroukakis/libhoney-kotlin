package io.honeycomb

/**
 * General configuration that can be applied to an [io.honeycomb.Event]
 * The class will initialise with apiHost and sampleRate set to the default values of
 * `https://api.honeycomb.io` and `1` respectively. The user is expected to provide a valid
 * [writeKey] and [dataSet]
 *
 * @constructor
 */
class HoneyConfig(
        val writeKey: String,
        val dataSet: String,
        val apiHost: String = "https://api.honeycomb.io",
        val sampleRate: Int = 1)
