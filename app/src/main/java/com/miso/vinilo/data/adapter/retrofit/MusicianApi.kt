package com.miso.vinilo.data.adapter.retrofit

import com.miso.vinilo.data.dto.MusicianDto
import retrofit2.http.GET

interface MusicianApi {
    @GET("musicians")
    suspend fun getMusicians(): List<MusicianDto>
}