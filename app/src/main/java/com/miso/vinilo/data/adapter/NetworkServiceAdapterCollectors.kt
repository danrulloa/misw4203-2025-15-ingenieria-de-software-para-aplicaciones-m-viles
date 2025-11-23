package com.miso.vinilo.data.adapter

import com.miso.vinilo.data.adapter.retrofit.CollectorApi
import com.miso.vinilo.data.dto.CollectorDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Network service adapter that uses a network API to retrieve collector data.
 * The dispatcher used for IO is injected so tests can pass a TestDispatcher.
 * @property collectorApi The [com.miso.vinilo.data.adapter.retrofit.CollectorApi] instance used to perform network requests.
 * @property ioDispatcher Dispatcher used for IO operations (defaults to [Dispatchers.IO]).
 */
class NetworkServiceAdapterCollectors(
    private val collectorApi: CollectorApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Fetches a list of collectors from the network API.
     * Executes the call on the injected IO dispatcher to avoid blocking the UI thread.
     * @return A [NetworkResult] containing a list of [CollectorDto] on success or an [Error].
     */
    suspend fun getCollectors(): NetworkResult<List<CollectorDto>> {
        return try {
            val dtos: List<CollectorDto> = withContext(ioDispatcher) {
                collectorApi.getCollectors()
            }
            NetworkResult.Success(dtos)
        } catch (e: Exception) {
            NetworkResult.Error("Unknown network error", e)
        }
    }

    /**
     * Fetches a single collector from the network API.
     * Executes the call on the injected IO dispatcher.
     * @return A [NetworkResult] containing a [CollectorDto] on success or an [Error].
     */
    suspend fun getCollector(id: Long): NetworkResult<CollectorDto> {
        return try {
            val dto: CollectorDto = withContext(ioDispatcher) {
                collectorApi.getCollector(id)
            }
            NetworkResult.Success(dto)
        } catch (e: Exception) {
            NetworkResult.Error("Unknown network error", e)
        }
    }

    companion object {
        fun create(baseUrl: String, ioDispatcher: CoroutineDispatcher = Dispatchers.IO): NetworkServiceAdapterCollectors {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
            val collectorApi = retrofit.create(CollectorApi::class.java)
            return NetworkServiceAdapterCollectors(collectorApi, ioDispatcher)
        }
    }
}
