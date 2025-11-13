package com.miso.vinilo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.repository.MusicianRepository
import com.miso.vinilo.data.adapter.NetworkConfig
import com.miso.vinilo.data.dto.AlbumDto
import kotlinx.coroutines.launch

/**
 * ViewModel that exposes musician list state to the UI.
 * It now depends directly on [MusicianRepository].
 */
class MusicianViewModel(
    private val repository: MusicianRepository
) : ViewModel() {

    private val _state = MutableLiveData<UiState>(UiState.Idle)
    val state: LiveData<UiState> = _state

    // Removed eager fetch from init: the UI should explicitly request data.

    /**
     * Triggers a network load of musicians and updates `state` accordingly.
     * This method must be called by the UI (or coordination layer) when data is required.
     */
    fun loadMusicians() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val result = repository.getMusicians()) {
                is NetworkResult.Success -> _state.value = UiState.Success(result.data)
                is NetworkResult.Error -> _state.value = UiState.Error(result.message)
            }
        }
    }

    private val _detailState = MutableLiveData<DetailUiState>(DetailUiState.Idle)
    val detailState: LiveData<DetailUiState> = _detailState

    fun loadMusician(id: Long) {
        viewModelScope.launch {
            _detailState.value = DetailUiState.Loading
            when (val result = repository.getMusician(id)) {
                is NetworkResult.Success -> {
                    val musician = result.data
                    val albumsUi = musician.albums.toAlbumUi()
                    _detailState.value = DetailUiState.Success(
                        DetailUiData(musician = musician, albums = albumsUi)
                    )
                }
                is NetworkResult.Error -> {
                    _detailState.value = DetailUiState.Error(result.message)
                }
            }
        }
    }


    /**
     * UI-friendly sealed class representing Idle/Loading/Success/Error states.
     */
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val data: List<MusicianDto>) : UiState()
        data class Error(val message: String) : UiState()
    }

    data class AlbumUi(
        val id: Long,
        val name: String,
        val cover: String?,
        val year: String
    )
    data class DetailUiData(
        val musician: MusicianDto,
        val albums: List<AlbumUi>
    )
    sealed class DetailUiState {
        object Idle : DetailUiState()
        object Loading : DetailUiState()
        data class Success(val data: DetailUiData) : DetailUiState()
        data class Error(val message: String) : DetailUiState()
    }

    // ===== Helpers =====
    private fun List<AlbumDto>.toAlbumUi(): List<AlbumUi> = map {
        AlbumUi(
            id = it.id,
            name = it.name,
            cover = it.cover,
            year = it.releaseDate?.take(4) ?: "—"
        )
    }

    /**
     * Convenience secondary constructor to quickly create a ViewModel wired to the network
     * implementation. Prefer passing a repository in production (DI) or tests.
     */
    constructor(baseUrl: String) : this(MusicianRepository.create(baseUrl))

    /**
     * No-arg constructor so the default ViewModelProvider (or Compose's viewModel()) can
     * instantiate this ViewModel without a factory. It now delegates to the repository
     * created from the mutable NetworkConfig so tests can override the base URL at runtime.
     */
    constructor() : this(MusicianRepository.create(NetworkConfig.baseUrl))
}