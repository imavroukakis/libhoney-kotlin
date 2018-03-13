# libhoney for Kotlin

[![CircleCI](https://circleci.com/gh/imavroukakis/libhoney-kotlin.svg?style=shield)](https://circleci.com/gh/imavroukakis/libhoney-kotlin)

Kotlin library for sending events to [Honeycomb](https://honeycomb.io).

## Installation:


## Example

Honeycomb can calculate all sorts of statistics, so send the values you care about and let us crunch the averages, percentiles, lower/upper bounds, cardinality -- whatever you want -- for you.

### Create Event
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


### Send event and block for result
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

### Send event async
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

## Contributions

Features, bug fixes and other changes to libhoney-kotlin are gladly accepted. Please
open issues or a pull request with your change. Remember to add your name to the
CONTRIBUTORS file!

All contributions will be released under the Apache License 2.0.
