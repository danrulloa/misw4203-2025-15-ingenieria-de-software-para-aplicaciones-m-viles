package com.miso.vinilo.data.adapter.retrofit

import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.dto.MusicianDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MusicianApi {
    @GET("musicians")
    suspend fun getMusicians(): List<MusicianDto>

    @GET("musicians/{id}")
    suspend fun getMusician(@Path("id") id: Long): MusicianDto

    @POST("musicians/{musicianId}/albums/{albumId}")
    suspend fun addAlbumToMusician(
        @Path("musicianId") musicianId: Long,
        @Path("albumId") albumId: Long
    ): AlbumDto

}