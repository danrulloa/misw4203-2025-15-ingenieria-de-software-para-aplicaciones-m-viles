package com.miso.vinilo.data.repository

import android.content.Context
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterAlbums
import com.miso.vinilo.data.database.ViniloDatabase
import com.miso.vinilo.data.database.dao.AlbumDao
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.mappers.toDto
import com.miso.vinilo.data.mappers.toEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlbumRepository(
    private val serviceAdapter: NetworkServiceAdapterAlbums,
    private val albumDao: AlbumDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {


    suspend fun getAlbums(forceRefresh: Boolean = false): NetworkResult<List<AlbumDto>> {
        return withContext(ioDispatcher) {
            try {
                if (!forceRefresh) {
                    val local = albumDao.getAll()
                    if (local.isNotEmpty()) {
                        return@withContext NetworkResult.Success(local.map { it.toDto() })
                    }
                }
                when (val net = serviceAdapter.getAlbums()) {
                    is NetworkResult.Success -> {
                        albumDao.clearAll()
                        albumDao.insertAll(net.data.map { it.toEntity() })
                        NetworkResult.Success(net.data)
                    }

                    is NetworkResult.Error -> {
                        val fallback = albumDao.getAll()
                        if (fallback.isNotEmpty()) {
                            NetworkResult.Success(fallback.map { it.toDto() })
                        } else {
                            net
                        }
                    }
                }
            } catch (e: Exception) {
                NetworkResult.Error("Unexpected error", e)
            }
        }
    }

    suspend fun getAlbum(id: Long): NetworkResult<AlbumDto> {
        return withContext(ioDispatcher) {
            try {
                val local = albumDao.getById(id)
                if (local != null) {
                    return@withContext NetworkResult.Success(local.toDto())
                }

                when (val net = serviceAdapter.getAlbum(id)) {
                    is NetworkResult.Success -> {
                        albumDao.insert(net.data.toEntity())
                        NetworkResult.Success(net.data)
                    }

                    is NetworkResult.Error -> net
                }
            } catch (e: Exception) {
                NetworkResult.Error("Unexpected error", e)
            }
        }
    }

    companion object {
        fun create(context: Context, baseUrl: String): AlbumRepository {
            val db = ViniloDatabase.getDatabase(context.applicationContext)
            val serviceAdapter = NetworkServiceAdapterAlbums.create(baseUrl)
            return AlbumRepository(serviceAdapter, db.albumDao())
        }
    }
}