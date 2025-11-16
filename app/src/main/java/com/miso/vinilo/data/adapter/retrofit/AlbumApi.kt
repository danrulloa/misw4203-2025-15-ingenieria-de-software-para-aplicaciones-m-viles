package com.miso.vinilo.data.adapter.retrofit

import com.miso.vinilo.data.dto.AlbumDto
import retrofit2.http.GET
import retrofit2.http.Path

interface AlbumApi {
    @GET("albums")
    suspend fun getAlbums(): List<AlbumDto>

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") id: Long): AlbumDto
}