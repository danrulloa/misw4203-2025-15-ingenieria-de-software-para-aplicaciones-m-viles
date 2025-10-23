package com.miso.vinilo.data.dto

import com.miso.vinilo.data.model.Genre
import com.miso.vinilo.data.model.RecordLabel
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlbumDto(
    val id: Long,
    val name: String,
    val cover: String,
    val releaseDate: String,  // Usar String para evitar problemas de parsing de fechas
    val description: String,
    val genre: Genre,
    val recordLabel: RecordLabel
)
