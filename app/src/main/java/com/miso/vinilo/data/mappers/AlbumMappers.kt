package com.miso.vinilo.data.mappers

import com.miso.vinilo.data.database.entities.AlbumEntity
import com.miso.vinilo.data.dto.AlbumDto

fun AlbumDto.toEntity(): AlbumEntity =
    AlbumEntity(
        id = this.id,
        name = this.name,
        cover = this.cover,
        releaseDate = this.releaseDate,
        description = this.description,
        genre = this.genre,
        recordLabel = this.recordLabel
    )


fun AlbumEntity.toDto(): AlbumDto =
    AlbumDto(
        id = this.id,
        name = this.name,
        cover = this.cover,
        releaseDate = this.releaseDate,
        description = this.description,
        genre = this.genre,
        recordLabel = this.recordLabel,
        tracks = emptyList(),
        performers = emptyList(),
        comments = emptyList(),
    )

fun List<AlbumDto>.toEntityList() = this.map { it.toEntity() }
fun List<AlbumEntity>.toDtoList() = this.map { it.toDto() }