package com.miso.vinilo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.repository.AlbumRepository
import kotlinx.coroutines.launch

/**
 * ViewModel that exposes album list state to the UI.
 * It now depends directly on [AlbumRepository].
 */
class AlbumViewModel(
    private val repository: AlbumRepository
) : ViewModel() {

    private val _state = MutableLiveData<UiState>(UiState.Idle)
    val state: LiveData<UiState> = _state

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

    /**
     * UI-friendly sealed class representing Idle/Loading/Success/Error states.
     */
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val data: List<AlbumDto>) : UiState()
        data class Error(val message: String) : UiState()
    }

}