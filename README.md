# libhoney for Kotlin

[![CircleCI](https://circleci.com/gh/imavroukakis/libhoney-kotlin.svg?style=shield)](https://circleci.com/gh/imavroukakis/libhoney-kotlin)
[![Download](https://api.bintray.com/packages/imavroukakis/maven/libhoney-kotlin/images/download.svg?version=0.1.1) ](https://bintray.com/imavroukakis/maven/libhoney-kotlin/0.1.1/link)[![codecov](https://codecov.io/gh/imavroukakis/libhoney-kotlin/branch/master/graph/badge.svg)](https://codecov.io/gh/imavroukakis/libhoney-kotlin)

Kotlin library for sending events to [Honeycomb](https://honeycomb.io).

## Installation:

### Gradle

```
repositories {
   jcenter()
}

compile 'io.honeycomb:libhoney-kotlin:<latest version>'
```


## Usage

Honeycomb can calculate all sorts of statistics, so send the values you care about and let us crunch the averages, percentiles, lower/upper bounds, cardinality -- whatever you want -- for you.

## Creating Events

### Using `HoneyConfig`

```
Event.newEvent(HoneyConfig(writeKey = "YOUR_KEY", dataSet = "test_data"), LocalDateTime.now())
     .add("string", "bar")
     .add("integer", 1)
     .add("float", 1.1f)
     .add("bool", true)
     .add("date", now)
     .add("array", listOf(1, 2, 3, 4))
     .add("range", 1..4)
```

### Standalone

```
Event.newEvent(writeKey = "write_key",dataSet = "your_data_set",timeStamp = LocalDateTime.now())
     .add("string", "bar")
     .add("integer", 1)
     .add("float", 1.1f)
     .add("bool", true)
     .add("date", now)
     .add("array", listOf(1, 2, 3, 4))
     .add("range", 1..4)
```


## Sending Events

### Send event and wait for response

```
val event =
    Event.newEvent(HoneyConfig(writeKey = "YOUR_KEY", dataSet = "test_data"), LocalDateTime.now())
      .add("string", "bar")
      .add("integer", 1)
      .add("float", 1.1f)
      .add("bool", true)
      .add("date", now)
      .add("array", listOf(1, 2, 3, 4))
      .add("range", 1..4)

val (_, response, _) = event.sendBlocking()
```

### Send event async

```
Event.newEvent(HoneyConfig(writeKey = "YOUR_KEY", dataSet = "test_data"), LocalDateTime.now())
     .add("string", "bar")
     .add("integer", 1)
     .add("float", 1.1f)
     .add("bool", true)
     .add("date", now)
     .add("array", listOf(1, 2, 3, 4))
     .add("range", 1..4)
     .send()
```

### Send event async and process response

```
val event =
    Event.newEvent(HoneyConfig(writeKey = "YOUR_KEY", dataSet = "test_data"), LocalDateTime.now())
      .add("string", "bar")
      .add("integer", 1)
      .add("float", 1.1f)
      .add("bool", true)
      .add("date", now)
      .add("array", listOf(1, 2, 3, 4))
      .add("range", 1..4)

event.send({ _, response, result ->
    result.fold({ _ ->
        println("$i -> ${response.statusCode}")
    }, { err ->
        println("$i -> Failure ${err.message}")
    })
})
```

### Batch send events

```
val event1 = Event.newEvent(honeyConfig, LocalDateTime.now())
                .add("string", "bar")
                .add("integer", 1)
                .add("float", 1.1f)
                .add("bool", true)
                .add("date", LocalDateTime.now())
                .add("array", listOf(1, 2, 3, 4))
                .add("range", 1..4)
val event2 = Event.newEvent(honeyConfig, LocalDateTime.now())
                .add("string", "bar")
                .add("integer", 1)
                .add("float", 1.1f)
                .add("bool", true)
                .add("date", LocalDateTime.now())
                .add("array", listOf(1, 2, 3, 4))
                .add("range", 1..4)
val (_, response, result) = listOf(event1, event2).blockingSend(honeyConfig)
assertThat(response.statusCode).isEqualTo(HttpURLConnection.HTTP_OK)
```

## Markers

### Create Marker

```
val marker = Marker(LocalDateTime.now().minusHours(1).toEpochSecond(ZoneOffset.UTC),
        LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
        message = "marker created",
        type = "integration test",
        url = "http://foo")
val result = marker.create(honeyConfig)
```

### Update marker

Using `result` from the _Create Marker_ example
```
val modifiedStartTime = LocalDateTime.now().minusHours(5).toEpochSecond(ZoneOffset.UTC)
val modifiedMarkerResult = Transmit.updateMarker(
        result.get()
        .copy(startTime = modifiedStartTime, message = "marker updated")
        .update(honeyConfig)
```

### Delete marker

Using `modifiedMarkerResult` from the _Update Marker_ example
```
val removedMarkerResult = modifiedMarkerResult.get().remove(honeyConfig)
```

### Get all markers

```
val allMarkers : List<Marker> = allMarkers(honeyConfig).get()
```

## Global fields

Use `GlobalConfig`, if you have common fields that you would like to send with every `Event`. They can either be constant, or dynamic (expressed as a function). Dynamic fields are evaluated right before the event is transmitted.

### Constant fields

```
GlobalConfig.addField("num", 1.0f)
```

### Dynamic fields

```
GlobalConfig.addField { Pair("heap_total", Runtime.getRuntime().totalMemory()) }
GlobalConfig.addField { Pair("heap_free", Runtime.getRuntime().freeMemory()) }
GlobalConfig.addField { Pair("time", LocalDateTime.now()) }
```

## Tuning

If you want to tweak the defaults you must set these **before you send your first Event**.

The following aspects of the library can be tuned

Parameter | Default Value
------------ | -------------
`Tuning.threadCount` | 20
`Tuning.maxQueueSize` | 1000
`Tuning.retryPolicy` | `Tuning.RetryPolicy.RETRY`

Through empirical testing:
- values between 20 and 30 work best for `threadCount`
- `Tuning.RetryPolicy.RETRY` is best if you want to ensure your events are delivered. Alternatively, if you don't mind trading consistency for speed, set `Tuning.retryPolicy` to `Tuning.RetryPolicy.DROP`

## Contributions

Features, bug fixes and other changes to libhoney-kotlin are gladly accepted. Please
open issues or a pull request with your change. Remember to add your name to the
CONTRIBUTORS file!

All contributions will be released under the Apache License 2.0.

## Credits

This project is made possible with the kind contribution of licenses from

- [JetBrains for IntelliJ IDE](https://www.jetbrains.com/)
- [EJ Technologies for Java Profiler](https://www.ej-technologies.com/products/jprofiler/overview.html)
