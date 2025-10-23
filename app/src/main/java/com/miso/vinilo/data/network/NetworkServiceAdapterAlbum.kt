package com.miso.vinilo.data.network

import com.miso.vinilo.data.model.Album
import com.miso.vinilo.data.network.retrofit.AlbumApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Interface that defines the service adapter used to fetch album data.
 */
interface AlbumServiceAdapter {
    suspend fun getAlbums(): NetworkResult<List<Album>>
}

/**
 * Implementation of [AlbumServiceAdapter] that uses a network API to retrieve album data.
 * @property api The [com.miso.vinilo.data.network.retrofit.AlbumApi] instance used to perform network requests.
 */
class NetworkServiceAdapterAlbums(private val api: AlbumApi) : AlbumServiceAdapter {

    /**
     * Fetches a list of albums from the network API.
     * @return A [NetworkResult] containing a list of [Album] on success or an [Error].
     */
    override suspend fun getAlbums(): NetworkResult<List<Album>> {
        return try {
            val dtos = api.getAlbums()
            val list = dtos.map { dto ->
                Album(
                    id = dto.id,
                    name = dto.name,
                    cover = dto.cover,
                    releaseDate = dto.releaseDate,
                    description = dto.description,
                    genre = dto.genre,
                    recordLabel = dto.recordLabel
                )
            }
            NetworkResult.Success(list)
        } catch (e: Exception) {
            NetworkResult.Error("Unknown network error", e)
        }
    }

    companion object {
        /**
         * Creates an instance of [AlbumServiceAdapter].
         * @param baseUrl The base URL for network requests.
         * @return A configured [AlbumServiceAdapter].
         */
        fun create(baseUrl: String): AlbumServiceAdapter {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val api = retrofit.create(AlbumApi::class.java)
            return NetworkServiceAdapterAlbums(api)
        }
    }
}
