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

    /**
     * Obtiene la lista de álbumes usando Room como caché.
     * - Si hay datos en Room y no se fuerza refresh → devuelve local.
     * - Si no hay local o se fuerza refresh → va a red, guarda en Room y devuelve.
     * - Si la red falla → intenta devolver lo que haya en Room (si existe).
     */
    suspend fun getAlbums(forceRefresh: Boolean = false): NetworkResult<List<AlbumDto>> {
        return withContext(ioDispatcher) {
            try {
                // 1. Intentar leer de Room si no se fuerza refresh
                if (!forceRefresh) {
                    val local = albumDao.getAll()
                    if (local.isNotEmpty()) {
                        return@withContext NetworkResult.Success(local.map { it.toDto() })
                    }
                }

                // 2. Llamar a la red
                when (val net = serviceAdapter.getAlbums()) {
                    is NetworkResult.Success -> {
                        // Guardar en Room
                        albumDao.clearAll()
                        albumDao.insertAll(net.data.map { it.toEntity() })
                        NetworkResult.Success(net.data)
                    }

                    is NetworkResult.Error -> {
                        // 3. Si hay error de red, intentar devolver lo que haya en Room
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

    /**
     * Obtiene un álbum:
     * - Primero intenta buscarlo en Room.
     * - Si no está, va a la red, lo guarda en Room y lo retorna.
     */
    suspend fun getAlbum(id: Long): NetworkResult<AlbumDto> {
        return withContext(ioDispatcher) {
            try {
                // 1. Buscar primero en Room
                val local = albumDao.getById(id)
                if (local != null) {
                    return@withContext NetworkResult.Success(local.toDto())
                }

                // 2. Si no está en Room, ir a la red
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
        /**
         * Convenience factory para crear el repositorio
         * conectado tanto al adapter de red como a Room.
         *
         * @param context Contexto de la app (para obtener la DB).
         * @param baseUrl Base URL del servicio Retrofit (e.g. "http://10.0.2.2:3000/")
         */
        fun create(context: Context, baseUrl: String): AlbumRepository {
            val db = ViniloDatabase.getDatabase(context.applicationContext)
            val serviceAdapter = NetworkServiceAdapterAlbums.create(baseUrl)
            return AlbumRepository(serviceAdapter, db.albumDao())
        }
    }
}