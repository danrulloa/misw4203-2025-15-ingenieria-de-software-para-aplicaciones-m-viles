package com.miso.vinilo.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.miso.vinilo.data.dto.MusicianDto

@Entity(tableName = "musicians")
data class MusicianEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val image: String?,
    val description: String?,
    val birthDate: String?,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toDto(): MusicianDto {
        return MusicianDto(
            id = id,
            name = name,
            image = image,
            description = description,
            birthDate = birthDate
        )
    }

    companion object {
        fun fromDto(dto: MusicianDto): MusicianEntity {
            return MusicianEntity(
                id = dto.id,
                name = dto.name,
                image = dto.image,
                description = dto.description,
                birthDate = dto.birthDate
            )
        }
    }
}

