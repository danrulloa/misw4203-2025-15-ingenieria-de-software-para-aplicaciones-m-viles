package com.miso.vinilo.data.model

import com.squareup.moshi.Json

enum class Genre(val displayName: String) {
    @Json(name = "Classical")
    CLASSICAL("Classical"),

    @Json(name = "Salsa")
    SALSA("Salsa"),

    @Json(name = "Rock")
    ROCK("Rock"),

    @Json(name = "Folk")
    FOLK("Folk")
}