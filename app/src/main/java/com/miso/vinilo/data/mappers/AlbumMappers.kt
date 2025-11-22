package com.miso.vinilo.data.mappers

import com.miso.vinilo.data.database.entities.AlbumEntity
import com.miso.vinilo.data.dto.AlbumDto

fun AlbumDto.toEntity() = AlbumEntity(
    id = id,
    name = name,
    cover = cover,
    releaseDate = releaseDate,
    description = description,
    genre = genre,
    recordLabel = recordLabel
)

fun AlbumEntity.toDto() = AlbumDto(
    id = id,
    name = name,
    cover = cover,
    releaseDate = releaseDate,
    description = description,
    genre = genre,
    recordLabel = recordLabel,
    tracks = emptyList(),
    performers = emptyList()
)