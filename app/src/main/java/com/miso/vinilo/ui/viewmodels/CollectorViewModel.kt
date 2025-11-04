package com.miso.vinilo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miso.vinilo.data.dto.CollectorDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.repository.CollectorRepository
import com.miso.vinilo.data.adapter.NetworkConfig
import kotlinx.coroutines.launch

/**
 * ViewModel that exposes collector list state to the UI.
 * It now depends directly on [CollectorRepository].
 */
class CollectorViewModel(
    private val repository: CollectorRepository
) : ViewModel() {

    private val _state = MutableLiveData<UiState>(UiState.Idle)
    val state: LiveData<UiState> = _state

    // Removed eager fetch from init: the UI should explicitly request data.

    /**
     * Triggers a network load of collectors and updates `state` accordingly.
     * This method must be called by the UI (or coordination layer) when data is required.
     */
    fun loadCollectors() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val result = repository.getCollectors()) {
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
        data class Success(val data: List<CollectorDto>) : UiState()
        data class Error(val message: String) : UiState()
    }

    /**
     * Convenience secondary constructor to quickly create a ViewModel wired to the network
     * implementation. Prefer passing a repository in production (DI) or tests.
     */
    constructor(baseUrl: String) : this(CollectorRepository.create(baseUrl))

    /**
     * No-arg constructor so the default ViewModelProvider (or Compose's viewModel()) can
     * instantiate this ViewModel without a factory. It now delegates to the repository
     * created from the mutable NetworkConfig so tests can override the base URL at runtime.
     */
    constructor() : this(CollectorRepository.create(NetworkConfig.baseUrl))
}