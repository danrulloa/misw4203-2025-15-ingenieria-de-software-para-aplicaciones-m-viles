package com.miso.vinilo.data.adapter.retrofit

import com.miso.vinilo.data.dto.CollectorDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CollectorApi {
    @GET("collectors")
    suspend fun getCollectors(): List<CollectorDto>

    @GET("collectors/{id}")
    suspend fun getCollector(@Path("id") id: Long): CollectorDto
}
