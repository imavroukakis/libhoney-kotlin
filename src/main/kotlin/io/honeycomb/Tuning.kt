package io.honeycomb

class Tuning private constructor() {
    companion object {
        var threadCount: Int = 20
        var maxQueueSize: Int = 1000
        var retryPolicy: RetryPolicy = RetryPolicy.RETRY
    }

    enum class RetryPolicy {
        RETRY, DROP
    }
}
