package io.honeycomb

import io.honeycomb.net.allMarkers
import io.honeycomb.net.create
import io.honeycomb.net.remove
import io.honeycomb.net.update
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.numerics.shouldBeExactly
import io.kotlintest.matchers.string.shouldBeEqualIgnoringCase
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.FunSpec
import mu.KotlinLogging
import java.time.LocalDateTime
import java.time.ZoneOffset

class MarkerIntegrationTest : FunSpec({

    val logger = KotlinLogging.logger {}

    test("create marker") {
        val marker = Marker(
            LocalDateTime.now().minusHours(1).toEpochSecond(ZoneOffset.UTC),
            LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            message = "marker created",
            type = "integration test",
            url = "http://foo"
        )
        val result = marker.create(honeyConfig)
        result.component1() shouldNotBe null
        logger.info { "Created marker with id ${result.get().id}" }

        val modifiedStartTime = LocalDateTime.now().minusHours(5).toEpochSecond(ZoneOffset.UTC)
        val modifiedMarkerResult =
            result.get().copy(startTime = modifiedStartTime, message = "marker updated").update(honeyConfig)
        modifiedMarkerResult.component1() shouldNotBe null
        modifiedMarkerResult.get().endTime shouldBeExactly marker.endTime
        modifiedMarkerResult.get().message shouldBeEqualIgnoringCase "marker updated"
        modifiedMarkerResult.get().startTime shouldBeExactly modifiedStartTime
        logger.info { "Updated marker with id ${modifiedMarkerResult.get().id}" }

        val allMarkers = allMarkers(honeyConfig).get()
        allMarkers shouldHaveSize 1
        logger.info { "found ${allMarkers.size} markers" }

        modifiedMarkerResult.get().startTime shouldBeExactly modifiedStartTime

        val removedMarkerResult = modifiedMarkerResult.get().remove(honeyConfig)
        removedMarkerResult.component1() shouldNotBe null
        logger.info { "Removed marker with id ${removedMarkerResult.get().id}" }
    }
})
