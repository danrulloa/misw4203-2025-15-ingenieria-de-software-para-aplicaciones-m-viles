package com.miso.vinilo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.miso.vinilo.data.repository.MusicianRepository

/**
 * Factory to create [MusicianViewModel] with a provided [MusicianRepository].
 * Use this in Activities/Fragments when the ViewModel requires constructor params.
 */
class MusicianViewModelFactory(
    private val repository: MusicianRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicianViewModel::class.java)) {
            return MusicianViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}
