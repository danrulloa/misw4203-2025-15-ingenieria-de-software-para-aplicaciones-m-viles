package com.miso.vinilo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.miso.vinilo.data.dto.CollectorDto
import com.miso.vinilo.data.repository.CollectorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CollectorViewModel(private val repository: CollectorRepository) : ViewModel() {

    /**
     * Flow of paginated collectors, cached in ViewModel scope to survive configuration changes.
     */
    val collectors: Flow<PagingData<CollectorDto>> = repository.getPagedCollectors()
        .cachedIn(viewModelScope)

    /**
     * Triggers a refresh if data is considered stale. Called on screen init.
     */
    fun onScreenReady() {
        viewModelScope.launch {
            repository.refreshIfNeeded()
        }
    }

    /**
     * Forces a refresh from the network. Used for pull-to-refresh.
     */
    fun onForceRefresh() {
        viewModelScope.launch {
            repository.forceRefresh()
        }
    }
}
