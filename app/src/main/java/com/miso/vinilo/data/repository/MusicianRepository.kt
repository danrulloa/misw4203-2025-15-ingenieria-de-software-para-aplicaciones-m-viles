package com.miso.vinilo.data.repository

import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterMusicians

/**
 * Concrete repository that exposes musician-related data operations and delegates to a network adapter.
 * The previous interface `MusicianRepository` was removed; callers should use this concrete class directly.
 */
class MusicianRepository(
    private val serviceAdapter: NetworkServiceAdapterMusicians
) {

    suspend fun getMusicians(): NetworkResult<List<MusicianDto>> {
        return serviceAdapter.getMusicians()
    }

    companion object {
        /**
         * Convenience factory to create a repository wired with the network adapter.
         * @param baseUrl Base URL for the Retrofit service (e.g. "http://localhost:3000/")
         */
        fun create(baseUrl: String): MusicianRepository =
            MusicianRepository(NetworkServiceAdapterMusicians.create(baseUrl))
    }
}