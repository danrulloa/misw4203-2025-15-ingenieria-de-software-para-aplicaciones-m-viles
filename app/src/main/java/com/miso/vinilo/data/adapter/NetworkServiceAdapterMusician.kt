package com.miso.vinilo.data.adapter

import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.adapter.retrofit.MusicianApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Sealed class that represents the result of a network operation.
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : NetworkResult<Nothing>()
}

/**
 * Network service adapter that uses a network API to retrieve musician data.
 * The dispatcher used for IO is injected so tests can pass a TestDispatcher.
 * @property api The [MusicianApi] instance used to perform network requests.
 * @property ioDispatcher Dispatcher used for IO operations (defaults to [Dispatchers.IO]).
 */
class NetworkServiceAdapterMusicians(
    private val api: MusicianApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Fetches a list of musicians from the network API.
     * Executes the call on the injected IO dispatcher to avoid blocking the UI thread.
     * @return A [NetworkResult] containing a list of [MusicianDto] on success or an [Error].
     */
    suspend fun getMusicians(): NetworkResult<List<MusicianDto>> {
        return try {
            val dtos: List<MusicianDto> = withContext(ioDispatcher) {
                api.getMusicians()
            }
            NetworkResult.Success(dtos)
        } catch (e: Exception) {
            NetworkResult.Error("Unknown network error", e)
        }
    }

    companion object {
        /**
         * Creates an instance of [NetworkServiceAdapterMusicians].
         * @param baseUrl The base URL for network requests.
         * @return A configured [NetworkServiceAdapterMusicians].
         */
        fun create(baseUrl: String): NetworkServiceAdapterMusicians {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val api = retrofit.create(MusicianApi::class.java)
            return NetworkServiceAdapterMusicians(api)
        }
    }
}