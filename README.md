# libhoney for Kotlin

[![CircleCI](https://circleci.com/gh/imavroukakis/libhoney-kotlin.svg?style=shield)](https://circleci.com/gh/imavroukakis/libhoney-kotlin)
[ ![Download](https://api.bintray.com/packages/imavroukakis/maven/libhoney-kotlin/images/download.svg?version=0.0.2) ](https://bintray.com/imavroukakis/maven/libhoney-kotlin/0.0.2/link)

Kotlin library for sending events to [Honeycomb](https://honeycomb.io).

## Installation:

### Gradle
```
repositories {
    maven {
          url  "https://dl.bintray.com/imavroukakis/maven"
    }
}

compile 'io.honeycomb:libhoney-kotlin:0.1'
```


## Usage

Honeycomb can calculate all sorts of statistics, so send the values you care about and let us crunch the averages, percentiles, lower/upper bounds, cardinality -- whatever you want -- for you.

### Events
#### Using `HoneyConfig`
```kotlin
val event =
    Event.newEvent(HoneyConfig(writeKey = "YOUR_KEY", dataSet = "test_data"), LocalDateTime.now())
      .add("string", "bar")
      .add("integer", 1)
      .add("float", 1.1f)
      .add("bool", true)
      .add("date", now)
      .add("array", listOf(1, 2, 3, 4))
      .add("range", 1..4)
```
#### Standalone
```kotlin
val event =
    Event.newEvent(writeKey = "write_key",dataSet = "your_data_set",timeStamp = LocalDateTime.now())
      .add("string", "bar")
      .add("integer", 1)
      .add("float", 1.1f)
      .add("bool", true)
      .add("date", now)
      .add("array", listOf(1, 2, 3, 4))
      .add("range", 1..4)
```


#### Send event and block for result
```kotlin
val event =
    Event.newEvent(HoneyConfig(writeKey = "YOUR_KEY", dataSet = "test_data"), LocalDateTime.now())
      .add("string", "bar")
      .add("integer", 1)
      .add("float", 1.1f)
      .add("bool", true)
      .add("date", now)
      .add("array", listOf(1, 2, 3, 4))
      .add("range", 1..4)

val (_, response, _) = Transmit.sendBlocking(event)
```

#### Send event async
```kotlin
val event =
    Event.newEvent(HoneyConfig(writeKey = "YOUR_KEY", dataSet = "test_data"), LocalDateTime.now())
      .add("string", "bar")
      .add("integer", 1)
      .add("float", 1.1f)
      .add("bool", true)
      .add("date", now)
      .add("array", listOf(1, 2, 3, 4))
      .add("range", 1..4)

Transmit.send(event)
```

### Markers
#### Create Marker
```kotlin
val marker = Marker(LocalDateTime.now().minusHours(1).toEpochSecond(ZoneOffset.UTC),
        LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
        message = "marker created",
        type = "integration test",
        url = "http://foo")
val result = Transmit.createMarker(marker, honeyConfig)
```
#### Update marker
Using `result` from the _Create Marker_ example
```kotlin
val modifiedStartTime = LocalDateTime.now().minusHours(5).toEpochSecond(ZoneOffset.UTC)
val modifiedMarkerResult = Transmit.updateMarker(
        result.get().copy(startTime = modifiedStartTime, message = "marker updated"), honeyConfig)
```
#### Delete marker
Using `modifiedMarkerResult` from the _Update Marker_ example
```kotlin
val removedMarkerResult = Transmit.removeMarker(modifiedMarkerResult.get(), honeyConfig)
```
## Contributions

Features, bug fixes and other changes to libhoney-kotlin are gladly accepted. Please
open issues or a pull request with your change. Remember to add your name to the
CONTRIBUTORS file!

All contributions will be released under the Apache License 2.0.
