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

    /**
     * Silent background refresh (only if data is stale).
     * Doesn't show loading indicator.
     */
    private fun refreshInBackground() {
        viewModelScope.launch {
            repository.refreshIfNeeded()
        }
    }
}