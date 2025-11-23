package com.miso.vinilo.data.repository

import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterCollectors
import com.miso.vinilo.data.dto.CollectorDto

/**
 * Concrete repository that exposes collector-related data operations and delegates to a network adapter.
 * The previous interface `CollectorRepository` was removed; callers should use this concrete class directly.
 */
class CollectorRepository(
    private val serviceAdapter: NetworkServiceAdapterCollectors
) {

    suspend fun getCollectors(): NetworkResult<List<CollectorDto>> {
        return serviceAdapter.getCollectors()
    }

    suspend fun getCollectorDetail(id: Long): NetworkResult<CollectorDto> {
        return serviceAdapter.getCollectorDetail(id)
    }

    companion object {
        /**
         * Convenience factory to create a repository wired with the network adapter.
         * @param baseUrl Base URL for the Retrofit service (e.g. "http://localhost:3000/")
         */
        fun create(baseUrl: String): CollectorRepository =
            CollectorRepository(NetworkServiceAdapterCollectors.create(baseUrl))
    }
}