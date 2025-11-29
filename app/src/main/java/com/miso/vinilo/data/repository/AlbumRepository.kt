package com.miso.vinilo.data.repository

import android.content.Context
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterAlbums
import com.miso.vinilo.data.database.ViniloDatabase
import com.miso.vinilo.data.database.dao.AlbumDao
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.dto.CommentDto
import com.miso.vinilo.data.dto.NewCommentDto
import com.miso.vinilo.data.mappers.toDto
import com.miso.vinilo.data.mappers.toDtoList
import com.miso.vinilo.data.mappers.toEntity
import com.miso.vinilo.data.mappers.toEntityList

/**
 * Concrete repository that exposes album-related data operations and delegates to a network adapter.
 * The previous interface `AlbumRepository` was removed; callers should use this concrete class directly.
 */
class AlbumRepository(
    private val serviceAdapter: NetworkServiceAdapterAlbums,
    private val albumDao: AlbumDao
) {

    suspend fun getAlbums(forceRefresh: Boolean = false): NetworkResult<List<AlbumDto>> {
        return try {
            val cached = albumDao.getAlbums()

            if (cached.isNotEmpty() && !forceRefresh) {
                NetworkResult.Success(cached.toDtoList())
            } else {
                when (val result = serviceAdapter.getAlbums()) {
                    is NetworkResult.Success -> {
                        val dtos = result.data
                        albumDao.clearAlbums()
                        albumDao.insertAlbums(dtos.toEntityList())
                        NetworkResult.Success(dtos)
                    }
                    is NetworkResult.Error -> {
                        if (cached.isNotEmpty()) {
                            NetworkResult.Success(cached.toDtoList())
                        } else {
                            result
                        }
                    }
                }
            }
        } catch (e: Exception) {
            val cached = albumDao.getAlbums()
            if (cached.isNotEmpty()) {
                NetworkResult.Success(cached.toDtoList())
            } else {
                NetworkResult.Error("Error al obtener álbumes", e)
            }
        }
    }

    suspend fun getAlbum(id: Long): NetworkResult<AlbumDto> {
        return when (val result = serviceAdapter.getAlbum(id)) {
            is NetworkResult.Success -> {
                val dto = result.data
                albumDao.insertAlbums(listOf(dto.toEntity()))
                NetworkResult.Success(dto)
            }
            is NetworkResult.Error -> {
                val cached = albumDao.getAlbumById(id)
                if (cached != null) {
                    NetworkResult.Success(cached.toDto())
                } else {
                    result
                }
            }
        }
    }

    suspend fun postComment(albumId: Long, comment: NewCommentDto): NetworkResult<CommentDto> {
        return serviceAdapter.postComment(albumId, comment)
    }

    suspend fun getAlbum(id: Long): NetworkResult<AlbumDto> {
        return serviceAdapter.getAlbum(id)
    }

    companion object {
        /**
         * Convenience factory to create a repository wired with the network adapter.
         * @param baseUrl Base URL for the Retrofit service (e.g. "http://localhost:3000/")
         */
        fun create(context: Context, baseUrl: String): AlbumRepository {
            val db = ViniloDatabase.getDatabase(context.applicationContext)
            val adapter = NetworkServiceAdapterAlbums.create(baseUrl)
            return AlbumRepository(adapter, db.albumDao())
        }
    }
}
