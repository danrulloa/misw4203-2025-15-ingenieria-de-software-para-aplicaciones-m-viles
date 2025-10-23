package com.miso.vinilo.data.model

// Modelo que usará el repositorio/ViewModel
data class Album(
    val id: Long,
    val name: String,
    val cover: String,
    val releaseDate: String,
    val description: String,
    val genre: Genre,
    val recordLabel: RecordLabel
)