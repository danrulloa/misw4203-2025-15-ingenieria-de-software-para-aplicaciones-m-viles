package com.miso.vinilo.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CollectorDto(
    val id: Long,
    val name: String,
    val telephone: String,
    val email: String,
    val collectorAlbums: List<CollectorAlbumDto>? = null,
    val comments: List<CommentDto>? = null,
    val favoritePerformers: List<PerformerDto>? = null
)

@JsonClass(generateAdapter = true)
data class CollectorAlbumDto(
    val id: Long,
    val price: Int,
    val status: String
)

@JsonClass(generateAdapter = true)
data class CommentDto(
    val id: Long,
    val description: String,
    val rating: Int
)

@JsonClass(generateAdapter = true)
data class PerformerDto(
    val id: Long,
    val name: String,
    val image: String?,
    val description: String?,
    val birthDate: String? = null,
    val creationDate: String? = null
)
