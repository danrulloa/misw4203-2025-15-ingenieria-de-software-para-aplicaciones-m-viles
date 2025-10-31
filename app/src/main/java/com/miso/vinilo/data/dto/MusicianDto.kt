package com.miso.vinilo.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MusicianDto(
    val id: Long,
    val name: String,
    val image: String?,
    val description: String?,
    val birthDate: String?
)