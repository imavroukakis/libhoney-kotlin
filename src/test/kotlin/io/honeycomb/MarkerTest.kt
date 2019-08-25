package io.honeycomb

import io.kotlintest.matchers.string.shouldBeEqualIgnoringCase
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.FunSpec
import java.time.LocalDateTime
import java.time.ZoneOffset

class MarkerTest : FunSpec({

    test("new marker instance should be correctly configured") {
        val json = Marker(
            LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            message = "marker created",
            type = "test",
            url = "http://foo"
        ).toJson()
        json.long("start_time") shouldNotBe null
        json.long("end_time") shouldBe null
        json.string("message") shouldBeEqualIgnoringCase "marker created"
        json.string("type") shouldBeEqualIgnoringCase "test"
        json.string("url") shouldBeEqualIgnoringCase "http://foo"
    }

    test("marker defaults are sane") {
        val marker = Marker()
        marker.startTime shouldBe -1L
        marker.endTime shouldBe -1L
        marker.message shouldBeEqualIgnoringCase ""
        marker.type shouldBeEqualIgnoringCase ""
        marker.url shouldBeEqualIgnoringCase ""
        marker.createdAt shouldBeEqualIgnoringCase ""
        marker.updatedAt shouldBeEqualIgnoringCase ""
    }
})
