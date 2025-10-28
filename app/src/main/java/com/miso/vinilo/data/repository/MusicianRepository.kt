package com.miso.vinilo.data.repository

import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterMusicians

/**
 * Repository interface that exposes musician-related data operations.
 * The repository depends on a [NetworkServiceAdapterMusicians] for network access.
 */
interface MusicianRepository {
    /**
     * Retrieves a list of musicians. Returns a [NetworkResult] wrapping the list or an error.
     */
    suspend fun getMusicians(): NetworkResult<List<MusicianDto>>
}

/**
 * Default implementation of [MusicianRepository] that delegates to a [NetworkServiceAdapterMusicians].
 */
class MusicianRepositoryImpl(
    private val serviceAdapter: NetworkServiceAdapterMusicians
) : MusicianRepository {

    override suspend fun getMusicians(): NetworkResult<List<MusicianDto>> {
        return serviceAdapter.getMusicians()
    }

    companion object {
        /**
         * Convenience factory to create a repository wired with the network adapter.
         * @param baseUrl Base URL for the Retrofit service (e.g. "http://localhost:3000/")
         */
        fun create(baseUrl: String): MusicianRepository =
            MusicianRepositoryImpl(NetworkServiceAdapterMusicians.create(baseUrl))
    }
}