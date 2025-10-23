package com.miso.vinilo.domain

import com.miso.vinilo.data.model.Musician
import com.miso.vinilo.data.network.NetworkResult
import com.miso.vinilo.data.repository.MusicianRepository
import com.miso.vinilo.data.repository.MusicianRepositoryImpl

/**
 * Controller / Use-case layer that orchestrates musician-related operations.
 * This sits between ViewModel and Repository to follow MVVM + Clean separation.
 */
interface MusicianController {
    suspend fun getMusicians(): NetworkResult<List<Musician>>
}

class MusicianControllerImpl(
    private val repository: MusicianRepository
) : MusicianController {

    override suspend fun getMusicians(): NetworkResult<List<Musician>> = repository.getMusicians()

    companion object {
        /** Convenience factory to create a controller wired to the default network repository. */
        fun create(baseUrl: String): MusicianController =
            MusicianControllerImpl(MusicianRepositoryImpl.create(baseUrl))
    }
}

