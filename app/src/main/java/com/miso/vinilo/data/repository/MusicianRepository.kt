package com.miso.vinilo.data.repository

import com.miso.vinilo.data.model.Musician
import com.miso.vinilo.data.network.MusicianServiceAdapter
import com.miso.vinilo.data.network.NetworkResult
import com.miso.vinilo.data.network.NetworkServiceAdapterMusicians

/**
 * Repository interface that exposes musician-related data operations.
 * The repository depends on a [MusicianServiceAdapter] for network access.
 */
interface MusicianRepository {
    /**
     * Retrieves a list of musicians. Returns a [NetworkResult] wrapping the list or an error.
     */
    suspend fun getMusicians(): NetworkResult<List<Musician>>
}

/**
 * Default implementation of [MusicianRepository] that delegates to a [MusicianServiceAdapter].
 */
class MusicianRepositoryImpl(
    private val serviceAdapter: MusicianServiceAdapter
) : MusicianRepository {

    override suspend fun getMusicians(): NetworkResult<List<Musician>> {
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