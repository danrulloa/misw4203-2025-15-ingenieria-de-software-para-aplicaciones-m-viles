package com.miso.vinilo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.repository.AlbumRepository
import kotlinx.coroutines.launch

class AlbumViewModel(
    private val repository: AlbumRepository
) : ViewModel() {

    private val _state = MutableLiveData<UiState>(UiState.Idle)
    val state: LiveData<UiState> = _state

    private val _albumDetailState = MutableLiveData<AlbumDetailUiState>(AlbumDetailUiState.Idle)
    val albumDetailState: LiveData<AlbumDetailUiState> = _albumDetailState

    fun loadAlbums(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val result = repository.getAlbums(forceRefresh)) {
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
}


