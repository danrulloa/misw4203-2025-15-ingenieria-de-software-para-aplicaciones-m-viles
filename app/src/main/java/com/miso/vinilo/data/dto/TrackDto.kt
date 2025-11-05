package com.miso.vinilo.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TrackDto(
    val id: Long,
    val name: String,
    val duration: String
)
