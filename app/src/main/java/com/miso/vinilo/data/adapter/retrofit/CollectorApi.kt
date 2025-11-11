package com.miso.vinilo.data.adapter.retrofit

import com.miso.vinilo.data.dto.CollectorDto
import retrofit2.http.GET

interface CollectorApi {
    @GET("collectors")
    suspend fun getCollectors(): List<CollectorDto>
}
