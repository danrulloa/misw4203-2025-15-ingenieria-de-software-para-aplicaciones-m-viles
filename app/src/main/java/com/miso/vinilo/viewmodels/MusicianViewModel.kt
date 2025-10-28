package com.miso.vinilo.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.repository.MusicianRepository
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

    /**
     * UI-friendly sealed class representing Idle/Loading/Success/Error states.
     */
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val data: List<MusicianDto>) : UiState()
        data class Error(val message: String) : UiState()
    }

    /**
     * Convenience secondary constructor to quickly create a ViewModel wired to the network
     * implementation. Prefer passing a repository in production (DI) or tests.
     */
    constructor(baseUrl: String) : this(MusicianRepository.create(baseUrl))
}