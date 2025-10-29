package com.miso.vinilo.data.repository

import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterAlbums
import com.miso.vinilo.data.dto.AlbumDto

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

    companion object {
        /**
         * Convenience factory to create a repository wired with the network adapter.
         * @param baseUrl Base URL for the Retrofit service (e.g. "http://localhost:3000/")
         */
        fun create(baseUrl: String): AlbumRepository =
            AlbumRepository(NetworkServiceAdapterAlbums.create(baseUrl))
    }
}