package com.miso.vinilo.data.network

import com.miso.vinilo.data.model.Musician
import com.miso.vinilo.data.network.retrofit.MusicianApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Sealed class that represents the result of a network operation.
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : NetworkResult<Nothing>()
}

/**
 * Network service adapter that uses a network API to retrieve musician data.
 * Other classes should call this implementation directly.
 * @property api The [com.miso.vinilo.data.network.retrofit.MusicianApi] instance used to perform network requests.
 */
class NetworkServiceAdapterMusicians(private val api: MusicianApi) {

    /**
     * Fetches a list of musicians from the network API.
     * @return A [NetworkResult] containing a list of [Musician] on success or an [Error].
     */
    suspend fun getMusicians(): NetworkResult<List<Musician>> {
        return try {
            val dtos = api.getMusicians()
            val list = dtos.map { dto ->
                Musician(
                    id = dto.id,
                    name = dto.name,
                    image = dto.image,
                    description = dto.description,
                    birthDate = dto.birthDate
                )
            }
            NetworkResult.Success(list)
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