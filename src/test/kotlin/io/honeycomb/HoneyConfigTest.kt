package io.honeycomb

import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class HoneyConfigTest {


    @Test
    fun checksDefaults() {
        val dataSet = "data_set"
        val writeKey = "WRITE_KEY"
        assertThat("https://api.honeycomb.io", equalTo(HoneyConfig(writeKey, dataSet).apiHost))
        val honeyConfig = HoneyConfig(writeKey, "myDataSet", "https://foo.bar")
        assertThat("https://foo.bar", equalTo(honeyConfig.apiHost))
        assertThat("myDataSet", equalTo(honeyConfig.dataSet))
    }
}
