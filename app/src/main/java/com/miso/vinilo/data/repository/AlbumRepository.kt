package com.miso.vinilo.data.repository

import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterAlbums
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.dto.CommentDto
import com.miso.vinilo.data.dto.NewCommentDto

/**
 * Concrete repository that exposes album-related data operations and delegates to a network adapter.
 * The previous interface `AlbumRepository` was removed; callers should use this concrete class directly.
 */
class AlbumRepository(
    private val serviceAdapter: NetworkServiceAdapterAlbums
) {

    suspend fun getAlbums(): NetworkResult<List<AlbumDto>> {
        return serviceAdapter.getAlbums()
    }

    suspend fun getAlbum(id: Long): NetworkResult<AlbumDto> {
        return serviceAdapter.getAlbum(id)
    }

    suspend fun postComment(albumId: Long, comment: NewCommentDto): NetworkResult<CommentDto> {
        return serviceAdapter.postComment(albumId, comment)
    }

    companion object {
        /**
         * Convenience factory to create a repository wired with the network adapter.
         * @param baseUrl Base URL for the Retrofit service (e.g. "http://localhost:3000/")
         */
        fun create(baseUrl: String): AlbumRepository =
            AlbumRepository(NetworkServiceAdapterAlbums.create(baseUrl))
    }
}
