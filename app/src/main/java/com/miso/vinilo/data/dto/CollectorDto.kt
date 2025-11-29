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
    val favoritePerformers: List<PerformerDto>? = null,
) {
    @Transient
    var albumCountForUi: Int? = null
}
