package com.miso.vinilo.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterCollectors
import com.miso.vinilo.data.database.dao.CollectorDao
import com.miso.vinilo.data.database.entities.CollectorEntity
import com.miso.vinilo.data.dto.CollectorDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CollectorRepository(
    private val serviceAdapter: NetworkServiceAdapterCollectors,
    private val collectorDao: CollectorDao
) {

    fun getPagedCollectors(): Flow<PagingData<CollectorDto>> {
        return Pager(
            config = PagingConfig(
                pageSize = 9,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { collectorDao.getPagedCollectors() }
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toDto() }
        }
    }

    suspend fun refreshIfNeeded() {
        val lastUpdate = collectorDao.getLastUpdateTime() ?: 0
        val elapsed = System.currentTimeMillis() - lastUpdate
        val thirtyMinutes = 1800000L

        if (elapsed > thirtyMinutes) {
            fetchAndStore()
        }
    }

    suspend fun forceRefresh() {
        fetchAndStore()
    }

    private suspend fun fetchAndStore() {
        when (val result = serviceAdapter.getCollectors()) {
            is NetworkResult.Success -> {
                val entities = result.data.map { CollectorEntity.fromDto(it) }
                collectorDao.deleteAll()
                collectorDao.insertAll(entities)
            }
            is NetworkResult.Error -> {
                // Silent fail
            }
        }
    }

    // Method from 'develop' for the detail screen
    suspend fun getCollectorDetail(id: Long): NetworkResult<CollectorDto> {
        return serviceAdapter.getCollector(id)
    }
}
