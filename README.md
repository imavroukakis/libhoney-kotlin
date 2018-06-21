# libhoney for Kotlin

[![CircleCI](https://circleci.com/gh/imavroukakis/libhoney-kotlin.svg?style=shield)](https://circleci.com/gh/imavroukakis/libhoney-kotlin)
[![Download](https://api.bintray.com/packages/imavroukakis/maven/libhoney-kotlin/images/download.svg?version=0.2.0) ](https://bintray.com/imavroukakis/maven/libhoney-kotlin/0.2.0/link)[![codecov](https://codecov.io/gh/imavroukakis/libhoney-kotlin/branch/master/graph/badge.svg)](https://codecov.io/gh/imavroukakis/libhoney-kotlin)

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

## Markers

### Dynamic fields

```
GlobalConfig.addField { Pair("heap_total", Runtime.getRuntime().totalMemory()) }
GlobalConfig.addField { Pair("heap_free", Runtime.getRuntime().freeMemory()) }
GlobalConfig.addField { Pair("time", LocalDateTime.now()) }
```

## Collection observability

A set of classes and extensions are provided that enable you to observe common collection operations. These are:

#### Classes
```
ObservableMutableLinkedList
ObservableMutableList
ObservableMutableMap
ObservableMutableSet
```

#### Collection extensions

```
add(element: E, honeyConfig: HoneyConfig): Boolean

addAll(elements: Collection<E>, honeyConfig: HoneyConfig): Boolean

removeAll(elements: Collection<E>, honeyConfig: HoneyConfig): Boolean

remove(element: E, honeyConfig: HoneyConfig): Boolean

clear(honeyConfig: HoneyConfig)
```

#### Map extensions

```
put(key: K, value: V, honeyConfig: HoneyConfig): V?

set(key: K, honeyConfig: HoneyConfig, value: V)

putAll(from: Map<out K, V>, honeyConfig: HoneyConfig)

remove(key: K, honeyConfig: HoneyConfig): V?

clear(honeyConfig: HoneyConfig)
```

### Usage

If you require the entire collection to be observed, use one from the `Observable` group and pass your `HoneyConfig` e.g.

`val list = ObservableMutableList<Int>(honeyConfig)`

Invoking any of `add`,`addAll`,`remove`,`removeAll` will result in an `Event` to be sent to Honeycomb. In the preceding example,invoking `add(4)` on the collection has the effect of sending the following event:
```
{"traceable-mutable-list.add": 4, "element-type": "java.lang.Integer"}
```

If you wish to selectively observe a collection or map, you can use any of the extensions functions e.g. `list.add(4, honeyConfig)`. The only requirement here is that you need to pass your `HoneyConfig` every time.

For some simple examples, look at `MutableCollectionExtensionsIntegrationTest` and `TraceableMutableCollectionsIntegrationTest` in the _integration-test_ folder.

## Tuning

If you want to tweak the defaults you must set these **before you send your first Event**.

The following aspects of the library can be tuned

Parameter             | Default Value
--------------------- | -------------
`Tuning.threadCount`  | 20
`Tuning.maxQueueSize` | 1000
`Tuning.retryPolicy`  | `Tuning.RetryPolicy.RETRY`

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
