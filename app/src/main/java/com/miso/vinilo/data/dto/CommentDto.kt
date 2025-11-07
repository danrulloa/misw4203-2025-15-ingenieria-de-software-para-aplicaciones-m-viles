package com.miso.vinilo.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentDto(
    val id: Long,
    val description: String,
    val rating: Int
)
