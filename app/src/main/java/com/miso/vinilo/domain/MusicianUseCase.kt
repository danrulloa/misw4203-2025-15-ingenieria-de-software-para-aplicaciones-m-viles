package com.miso.vinilo.domain

import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.repository.MusicianRepository

/**
 * Use-case layer that orchestrates musician-related operations.
 * This sits between ViewModel and Repository to follow MVVM + Clean separation.
 */
interface MusicianUseCase {
    suspend fun getMusicians(): NetworkResult<List<MusicianDto>>
}

class MusicianUseCaseImpl(
    private val repository: MusicianRepository
) : MusicianUseCase {

    override suspend fun getMusicians(): NetworkResult<List<MusicianDto>> = repository.getMusicians()

    companion object {
        /** Convenience factory to create a controller wired to the default network repository. */
        fun create(baseUrl: String): MusicianUseCase =
            MusicianUseCaseImpl(MusicianRepository.create(baseUrl))
    }
}
