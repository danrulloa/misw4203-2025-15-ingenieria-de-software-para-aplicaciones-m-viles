package com.miso.vinilo.data.adapter

import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.dto.CommentDto
import com.miso.vinilo.data.dto.NewCommentDto
import com.miso.vinilo.data.adapter.retrofit.AlbumApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val TAG = "vinilo"

/**
 * Network service adapter that uses a network API to retrieve album data.
 * Other classes should call this implementation directly.
 * @property api The [com.miso.vinilo.data.adapter.retrofit.AlbumApi] instance used to perform network requests.
 * @property ioDispatcher The dispatcher used for IO operations (defaults to [Dispatchers.IO]).
 */
class NetworkServiceAdapterAlbums(
    private val api: AlbumApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Fetches a list of albums from the network API.
     * @return A [NetworkResult] containing a list of [AlbumDto] on success or an [Error].
     */
    suspend fun getAlbums(): NetworkResult<List<AlbumDto>> {
        return try {
            val dtos = withContext(ioDispatcher) { 
                api.getAlbums()
            }
            NetworkResult.Success(dtos)
        } catch (e: Exception) {
            NetworkResult.Error("Unknown network error", e)
        }
    }

    /**
     * Fetches a single album from the network API.
     * @param id The ID of the album to fetch.
     * @return A [NetworkResult] containing an [AlbumDto] on success or an [Error].
     */
    suspend fun getAlbum(id: Long): NetworkResult<AlbumDto> {
        return try {
            val dto = withContext(ioDispatcher) {
                api.getAlbum(id)
            }
            NetworkResult.Success(dto)
        } catch (e: Exception) {
            NetworkResult.Error("Unknown network error", e)
        }
    }

    /**
     * Posts a new comment to a specific album.
     * @param albumId The ID of the album to post the comment to.
     * @param comment The new comment to post.
     * @return A [NetworkResult] containing the created [CommentDto] on success or an [Error].
     */
    suspend fun postComment(albumId: Long, comment: NewCommentDto): NetworkResult<CommentDto> {
        return try {
            val createdComment = withContext(ioDispatcher) {
                api.postComment(albumId, comment)
            }
            NetworkResult.Success(createdComment)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to post comment", e)
        }
    }

    companion object {
        /**
         * Creates an instance of [NetworkServiceAdapterAlbums].
         * @param baseUrl The base URL for network requests.
         * @return A configured [com.miso.vinilo.data.adapter.NetworkServiceAdapterAlbums].
         */
        fun create(baseUrl: String): NetworkServiceAdapterAlbums {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val api = retrofit.create(AlbumApi::class.java)
            // The ioDispatcher will use its default value (Dispatchers.IO)
            return NetworkServiceAdapterAlbums(api)
        }
    }
}
