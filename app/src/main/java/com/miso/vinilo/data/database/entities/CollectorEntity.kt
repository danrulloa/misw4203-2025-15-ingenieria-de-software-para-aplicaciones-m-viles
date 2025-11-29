package com.miso.vinilo.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.miso.vinilo.data.dto.CollectorDto

@Entity(tableName = "collectors")
data class CollectorEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val telephone: String,
    val email: String,
    val albumCount: Int, // New field
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toDto(): CollectorDto {
        val dto = CollectorDto(
            id = id,
            name = name,
            telephone = telephone,
            email = email
        )
        // Populate the transient field for UI
        dto.albumCountForUi = albumCount
        return dto
    }

    companion object {
        fun fromDto(dto: CollectorDto) = CollectorEntity(
            id = dto.id,
            name = dto.name,
            telephone = dto.telephone,
            email = dto.email,
            // Calculate and store the album count
            albumCount = dto.collectorAlbums?.size ?: 0
        )
    }
}
