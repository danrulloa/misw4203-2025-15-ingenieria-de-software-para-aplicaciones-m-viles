package com.miso.vinilo.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlbumDto(
    val id: Long,
    val name: String,
    val cover: String,
    val releaseDate: String,
    val description: String,
    val genre: String,
    val recordLabel: String,
    val tracks: List<TrackDto>?,
    val performers: List<PerformerDto>?
)
