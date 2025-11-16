package com.miso.vinilo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.repository.MusicianRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.dto.AlbumDto

/**
 * ViewModel that exposes paginated musician data with pull-to-refresh support.
 * Uses hybrid strategy: shows Room data immediately + refreshes in background.
 */
class MusicianViewModel(
    private val repository: MusicianRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /**
     * Flow of paginated musicians (9 per page).
     * cachedIn ensures the data survives configuration changes.
     */
    val musicians: Flow<PagingData<MusicianDto>> = repository.getPagedMusicians()
        .cachedIn(viewModelScope)

    init {
        // Automatic background refresh on initialization
        refreshInBackground()
    }

    /**
     * Manual refresh triggered by pull-to-refresh.
     * Forces a network call and updates the refresh indicator.
     */
    fun refreshMusicians() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.forceRefresh()
            } finally {
                _isRefreshing.value = false
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
                    val albumsUi = musician.albums.map {
                        AlbumUi(
                            id = it.id,
                            name = it.name,
                            cover = it.cover,
                            year = it.releaseDate?.take(4) ?: "—"
                        )
                    }
                    _detailState.value = DetailUiState.Success(
                        DetailUiData(
                            musician = musician,
                            albums = albumsUi
                        )
                    )
                }
                is NetworkResult.Error ->
                    _detailState.value = DetailUiState.Error(result.message)
            }
        }
    }



    /**
     * Silent background refresh (only if data is stale).
     * Doesn't show loading indicator.
     */
    private fun refreshInBackground() {
        viewModelScope.launch {
            repository.refreshIfNeeded()
        }
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

    // NOTE: ViewModel instances should be created by DI (Koin) or by providing a Repository.
    // Secondary convenience constructors were removed because repository creation requires
    // a MusicianDao instance (Room) which is not available here.
}