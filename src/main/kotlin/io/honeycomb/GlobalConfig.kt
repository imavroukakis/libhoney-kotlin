package io.honeycomb

/**
 * Global object that holds additional key/value pairs that will be added to an [Event] before
 * it is transmitted.
 */
object GlobalConfig {
    val dataPairs = mutableMapOf<String, Any>()
}
