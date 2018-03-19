package io.honeycomb

import com.beust.klaxon.Json

data class Marker(
        @Json(name = "start_time")
        val startTime: Long?,
        @Json(name = "end_time")
        val endTime: Long? = null,
        val message: String = "",
        val type: String = "",
        val url: String = "",
        @Json(name = "created_at")
        val createdAt: String = "",
        @Json(name = "updated_at")
        val updatedAt: String = "",
        val id: String = "")
