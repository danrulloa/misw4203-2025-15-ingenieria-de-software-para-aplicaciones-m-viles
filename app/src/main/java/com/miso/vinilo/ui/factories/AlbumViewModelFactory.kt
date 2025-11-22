package com.miso.vinilo.ui.factories

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.miso.vinilo.data.adapter.NetworkConfig
import com.miso.vinilo.data.repository.AlbumRepository
import com.miso.vinilo.ui.viewmodels.AlbumViewModel

class AlbumViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlbumViewModel::class.java)) {
            val repo = AlbumRepository.create(
                context = context.applicationContext,
                baseUrl = NetworkConfig.baseUrl
            )
            @Suppress("UNCHECKED_CAST")
            return AlbumViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
