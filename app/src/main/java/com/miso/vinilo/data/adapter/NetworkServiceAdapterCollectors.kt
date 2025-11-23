package com.miso.vinilo.data.adapter

import com.miso.vinilo.data.adapter.retrofit.CollectorApi
import com.miso.vinilo.data.dto.CollectorDto
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkServiceAdapterCollectors(private val collectorApi: CollectorApi) {

    suspend fun getCollectors(): NetworkResult<List<CollectorDto>> {
        return try {
            val collectors = collectorApi.getCollectors()
            NetworkResult.Success(collectors)
        } catch (e: Exception) {
            NetworkResult.Error("Unknown network error",e)
        }
    }

    suspend fun getCollector(id: Long): NetworkResult<CollectorDto> {
        return try {
            val collector = collectorApi.getCollector(id)
            NetworkResult.Success(collector)
        } catch (e: Exception) {
            NetworkResult.Error("Unknown network error",e)
        }
    }

    companion object {
        fun create(baseUrl: String): NetworkServiceAdapterCollectors {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            val collectorApi = retrofit.create(CollectorApi::class.java)
            return NetworkServiceAdapterCollectors(collectorApi)
        }
    }
}
