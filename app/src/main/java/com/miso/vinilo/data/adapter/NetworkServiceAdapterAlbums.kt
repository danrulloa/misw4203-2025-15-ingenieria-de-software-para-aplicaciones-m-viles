package com.miso.vinilo.data.adapter

import android.util.Log
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.adapter.retrofit.AlbumApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val TAG = "NetworkServiceAdapter"

class NetworkServiceAdapterAlbums(private val api: AlbumApi) {

    suspend fun getAlbums(): NetworkResult<List<AlbumDto>> {
        return try {
            val dtos: List<AlbumDto> = api.getAlbums()
            NetworkResult.Success(dtos)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting albums: ", e)
            NetworkResult.Error("Unknown network error", e)
        }
    }

    suspend fun getAlbum(id: Long): NetworkResult<AlbumDto> {
        return try {
            val dto: AlbumDto = api.getAlbum(id)
            NetworkResult.Success(dto)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting album with id $id: ", e)
            NetworkResult.Error("Unknown network error", e)
        }
    }

    companion object {
        fun create(baseUrl: String): NetworkServiceAdapterAlbums {
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