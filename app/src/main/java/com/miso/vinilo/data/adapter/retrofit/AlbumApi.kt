package com.miso.vinilo.data.adapter.retrofit

import com.miso.vinilo.data.dto.AlbumDto
import retrofit2.http.GET

interface AlbumApi {
    @GET("albums")
    suspend fun getAlbums(): List<AlbumDto>
}