package com.miso.vinilo.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miso.vinilo.data.model.Musician
import com.miso.vinilo.data.network.NetworkResult
import com.miso.vinilo.domain.MusicianController
import com.miso.vinilo.domain.MusicianControllerImpl
import kotlinx.coroutines.launch

/**
 * ViewModel that exposes musician list state to the UI.
 * It depends on a `MusicianRepository` abstraction so it can be easily tested.
 */
class MusicianViewModel(
    private val controller: MusicianController
) : ViewModel() {

    private val _state = MutableLiveData<UiState>(UiState.Loading)
    val state: LiveData<UiState> = _state

    init {
        fetchMusicians()
    }

    /**
     * Triggers a network load of musicians and updates `state` accordingly.
     */
    fun fetchMusicians() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val result = controller.getMusicians()) {
                is NetworkResult.Success -> _state.value = UiState.Success(result.data)
                is NetworkResult.Error -> _state.value = UiState.Error(result.message)
            }
        }
    }

    /**
     * UI-friendly sealed class representing Loading/Success/Error states.
     */
    sealed class UiState {
        object Loading : UiState()
        data class Success(val data: List<Musician>) : UiState()
        data class Error(val message: String) : UiState()
    }

    /**
     * Convenience secondary constructor to quickly create a ViewModel wired to the network
     * implementation. Prefer passing a `MusicianController` in production (DI) or tests.
     */
    constructor(baseUrl: String) : this(MusicianControllerImpl.create(baseUrl))
}