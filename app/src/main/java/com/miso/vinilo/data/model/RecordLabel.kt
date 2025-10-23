package com.miso.vinilo.data.model

import com.squareup.moshi.Json

enum class RecordLabel(val displayName: String) {
    @Json(name = "Sony Music")
    SONY("Sony Music"),

    @Json(name = "EMI")
    EMI("EMI"),

    @Json(name = "Discos Fuentes")
    FUENTES("Discos Fuentes"),

    @Json(name = "Elektra")
    ELEKTRA("Elektra"),

    @Json(name = "Fania Records")
    FANIA("Fania Records")
}
