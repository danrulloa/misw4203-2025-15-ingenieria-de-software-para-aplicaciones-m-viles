package com.miso.vinilo.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterMusicians
import com.miso.vinilo.data.database.dao.MusicianDao
import com.miso.vinilo.data.database.entities.MusicianEntity
import com.miso.vinilo.data.dto.AlbumDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository that manages musicians data using Room as single source of truth
 * with network refresh capabilities (hybrid strategy).
 */
class MusicianRepository(
    private val serviceAdapter: NetworkServiceAdapterMusicians,
    private val musicianDao: MusicianDao
) {

    /**
     * Returns a Flow of paginated musicians from Room database.
     * Pages are loaded with 9 items each.
     */
    fun getPagedMusicians(): Flow<PagingData<MusicianDto>> {
        return Pager(
            config = PagingConfig(
                pageSize = 9,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { musicianDao.getPagedMusicians() }
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toDto() }
        }
    }

    /**
     * Refreshes musicians from network only if data is stale (> 30 minutes old).
     * This is called automatically on screen initialization (background refresh).
     */
    suspend fun refreshIfNeeded() {
        val lastUpdate = musicianDao.getLastUpdateTime() ?: 0
        val elapsed = System.currentTimeMillis() - lastUpdate
        val thirtyMinutes = 1800000L

        if (elapsed > thirtyMinutes) {
            fetchAndStore()
        }
    }

    /**
     * Forces a refresh from network regardless of cache age.
     * Used for manual pull-to-refresh.
     */
    suspend fun forceRefresh() {
        fetchAndStore()
    }

    /**
     * Internal method to fetch from network and store in Room.
     */
    private suspend fun fetchAndStore() {
        when (val result = serviceAdapter.getMusicians()) {
            is NetworkResult.Success -> {
                val entities = result.data.map { MusicianEntity.fromDto(it) }
                musicianDao.deleteAll()
                musicianDao.insertAll(entities)
                // entities already have lastUpdated timestamp set by MusicianEntity.fromDto()
            }
            is NetworkResult.Error -> {
                // Silent fail - Room data remains available
            }
        }
    }

    /**
     * Obtain a single musician's details from the network (used for detail screen).
     * Returning NetworkResult preserves errors to surface to UI if needed.
     */
    suspend fun getMusician(id: Long): NetworkResult<MusicianDto> {
        return serviceAdapter.getMusician(id)
    }

    /**
     * Associates an album with a musician via the network API.
     * @param musicianId The ID of the musician
     * @param albumId The ID of the album to associate
     * @return NetworkResult wrapping the updated AlbumDto on success, or an error
     */
    suspend fun addAlbumToMusician(
        musicianId: Long,
        albumId: Long
    ): NetworkResult<AlbumDto>{
        return serviceAdapter.addAlbumToMusician(musicianId,albumId)
    }
    companion object {
        /**
         * Convenience factory to create a repository wired with the network adapter.
         * @param baseUrl Base URL for the Retrofit service (e.g. "http://localhost:3000/")
         */
        fun create(baseUrl: String, musicianDao: MusicianDao): MusicianRepository =
            MusicianRepository(NetworkServiceAdapterMusicians.create(baseUrl), musicianDao)
    }
}