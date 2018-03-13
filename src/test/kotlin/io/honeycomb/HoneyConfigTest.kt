package io.honeycomb

import io.honeycomb.HoneyConfig
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class HoneyConfigTest {


    @Test
    fun checksDefaults() {
        var honeyConfig = HoneyConfig()
        assertThat("https://api.honeycomb.io", equalTo(honeyConfig.apiHost))

        honeyConfig = HoneyConfig("KEY","myDataSet")
        assertThat("https://api.honeycomb.io", equalTo(honeyConfig.apiHost))
        assertThat("KEY", equalTo(honeyConfig.writeKey))
        assertThat("myDataSet", equalTo(honeyConfig.dataSet))

        honeyConfig = HoneyConfig("KEY","myDataSet","https://foo.bar")
        assertThat("https://foo.bar", equalTo(honeyConfig.apiHost))
    }
}
