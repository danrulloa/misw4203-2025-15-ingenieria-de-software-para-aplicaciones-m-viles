package com.miso.vinilo.data.adapter

import com.miso.vinilo.data.dto.CollectorDto
import com.miso.vinilo.data.adapter.retrofit.CollectorApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Network service adapter that uses a network API to retrieve collector data.
 * The dispatcher used for IO is injected so tests can pass a TestDispatcher.
 * @property api The [com.miso.vinilo.data.adapter.retrofit.CollectorApi] instance used to perform network requests.
 * @property ioDispatcher Dispatcher used for IO operations (defaults to [Dispatchers.IO]).
 */
class NetworkServiceAdapterCollectors(
    private val api: CollectorApi,
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
                api.getCollectors()
            }
            NetworkResult.Success(dtos)
        } catch (e: Exception) {
            NetworkResult.Error("Unknown network error", e)
        }
    }

    companion object {
        /**
         * Creates an instance of [NetworkServiceAdapterCollectors].
         * @param baseUrl The base URL for network requests.
         * @return A configured [com.miso.vinilo.data.adapter.NetworkServiceAdapterCollectors].
         */
        fun create(baseUrl: String): NetworkServiceAdapterCollectors {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val api = retrofit.create(CollectorApi::class.java)
            return NetworkServiceAdapterCollectors(api)
        }
    }
}