package com.miso.vinilo.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CollectorAlbumDto(
    val id: Long,
    val price: Int,
    val status: String
)
