package com.miso.vinilo.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewCommentDto(
    val description: String,
    val rating: Int,
    val collector: CollectorIdDto
)
