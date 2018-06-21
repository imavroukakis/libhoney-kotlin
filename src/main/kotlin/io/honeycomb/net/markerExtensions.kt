package io.honeycomb.net

import com.beust.klaxon.Klaxon
import com.github.kittinunf.result.Result
import io.honeycomb.HoneyConfig
import io.honeycomb.Marker

/**
 * Creates a new [Marker]
 *
 * This is a _blocking_ request and you will need to handle the result
 *
 * @return the [Result], contains a new [Marker] instance additionally populated with the marker id or an exception
 * if the call failed
 */
fun Marker.create(honeyConfig: HoneyConfig): Result<Marker, Exception> {
    val (_, _, result) = Transmit.postMarker(this, honeyConfig).responseString()
    return when (result) {
        is Result.Failure -> {
            Result.error(result.error)
        }
        is Result.Success -> {
            Result.of(Klaxon().parse<Marker>(result.get()))
        }
    }
}

/**
 * Updates the [Marker]
 *
 * This is a _blocking_ request and you will need to handle the result
 *
 * @return the [Result], contains a new [Marker] instance with the updated marker data or an exception
 * if the call failed
 */
fun Marker.update(honeyConfig: HoneyConfig): Result<Marker, Exception> {
    val (_, _, result) = Transmit.putMarker(this, honeyConfig).responseString()
    return when (result) {
        is Result.Failure -> {
            Result.error(result.error)
        }
        is Result.Success -> {
            Result.of(Klaxon().parse<Marker>(result.get()))
        }
    }
}

/**
 * Deletes the [Marker]
 *
 * This is a _blocking_ request and you will need to handle the result
 *
 * @return the [Result], contains the deleted [Marker] instance or an exception if the call failed
 */
fun Marker.remove(honeyConfig: HoneyConfig): Result<Marker, Exception> {
    val (_, _, result) = Transmit.deleteMarker(this, honeyConfig).responseString()
    return when (result) {
        is Result.Failure -> {
            Result.error(result.error)
        }
        is Result.Success -> {
            Result.of(Klaxon().parse<Marker>(result.get()))
        }
    }
}

/**
 * Lists all existing [Marker]
 *
 * This is a _blocking_ request and you will need to handle the result
 *
 * @param honeyConfig the [HoneyConfig]
 *
 * @return the [Result], contains a list of [Marker] instances or an exception if the call failed
 */
fun allMarkers(honeyConfig: HoneyConfig): Result<List<Marker>, Exception> {
    val (_, _, result) = Transmit.getMarkers(honeyConfig).responseString()
    return when (result) {
        is Result.Failure -> {
            Result.error(result.error)
        }
        is Result.Success -> {
            Result.of(Klaxon().parseArray(result.get()))
        }
    }
}
