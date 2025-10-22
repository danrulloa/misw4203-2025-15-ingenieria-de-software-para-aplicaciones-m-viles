package com.miso.vinilo.data.model

// Modelo ligero que usará el repositorio/ViewModel
data class Musician(
    val id: Long,
    val name: String,
    val image: String?,
    val description: String?,
    val birthDate: String?
)