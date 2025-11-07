package com.miso.vinilo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.repository.AlbumRepository
import com.miso.vinilo.data.adapter.NetworkConfig
import kotlinx.coroutines.launch

/**
 * ViewModel that exposes album list state to the UI.
 * It now depends directly on [AlbumRepository].
 */
class  AlbumViewModel(
    private val repository: AlbumRepository
) : ViewModel() {

    private val _state = MutableLiveData<UiState>(UiState.Idle)
    val state: LiveData<UiState> = _state

    private val _albumDetailState = MutableLiveData<AlbumDetailUiState>(AlbumDetailUiState.Idle)
    val albumDetailState: LiveData<AlbumDetailUiState> = _albumDetailState

    // Removed eager fetch from init: the UI should explicitly request data.

    /**
     * Triggers a network load of albums and updates `state` accordingly.
     * This method must be called by the UI (or coordination layer) when data is required.
     */
    fun loadAlbums() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val result = repository.getAlbums()) {
                is NetworkResult.Success -> _state.value = UiState.Success(result.data)
                is NetworkResult.Error -> _state.value = UiState.Error(result.message)
            }
        }
    }

    fun loadAlbum(id: Long) {
        viewModelScope.launch {
            _albumDetailState.value = AlbumDetailUiState.Loading
            when (val result = repository.getAlbum(id)) {
                is NetworkResult.Success -> _albumDetailState.value = AlbumDetailUiState.Success(result.data)
                is NetworkResult.Error -> _albumDetailState.value = AlbumDetailUiState.Error(result.message)
            }
        }
    }

    /**
     * UI-friendly sealed class representing Idle/Loading/Success/Error states.
     */
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val data: List<AlbumDto>) : UiState()
        data class Error(val message: String) : UiState()
    }

    sealed class AlbumDetailUiState {
        object Idle : AlbumDetailUiState()
        object Loading : AlbumDetailUiState()
        data class Success(val data: AlbumDto) : AlbumDetailUiState()
        data class Error(val message: String) : AlbumDetailUiState()
    }

    /**
     * Convenience secondary constructor to quickly create a ViewModel wired to the network
     * implementation. Prefer passing a repository in production (DI) or tests.
     */
    constructor(baseUrl: String) : this(AlbumRepository.create(baseUrl))

    /**
     * No-arg constructor so the default ViewModelProvider (or Compose's viewModel()) can
     * instantiate this ViewModel without a factory. It now delegates to the repository
     * created from the mutable NetworkConfig so tests can override the base URL at runtime.
     */
    constructor() : this(AlbumRepository.create(NetworkConfig.baseUrl))
}