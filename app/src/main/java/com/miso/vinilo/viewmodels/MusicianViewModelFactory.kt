package com.miso.vinilo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.miso.vinilo.domain.MusicianController

/**
 * Factory to create [MusicianViewModel] with a provided [MusicianController].
 * Use this in Activities/Fragments when the ViewModel requires constructor params.
 */
class MusicianViewModelFactory(
    private val controller: MusicianController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicianViewModel::class.java)) {
            return MusicianViewModel(controller) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}
