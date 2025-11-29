package com.miso.vinilo.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.miso.vinilo.data.dto.CommentDto
import com.miso.vinilo.data.dto.PerformerDto
import com.miso.vinilo.data.dto.TrackDto


@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val cover: String,
    val releaseDate: String,
    val description: String,
    val genre: String,
    val recordLabel: String
)