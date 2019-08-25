package io.honeycomb

import io.kotlintest.matchers.string.shouldBeEqualIgnoringCase
import io.kotlintest.specs.FunSpec

class HoneyConfigTest : FunSpec({

    test("configuration defaults and assignment are sane") {
        val dataSet = "data_set"
        val writeKey = "WRITE_KEY"
        "https://api.honeycomb.io" shouldBeEqualIgnoringCase HoneyConfig(writeKey, dataSet).apiHost
        val honeyConfig = HoneyConfig(writeKey, "myDataSet", "https://foo.bar")
        "https://foo.bar" shouldBeEqualIgnoringCase honeyConfig.apiHost
        "myDataSet" shouldBeEqualIgnoringCase honeyConfig.dataSet
    }
})
