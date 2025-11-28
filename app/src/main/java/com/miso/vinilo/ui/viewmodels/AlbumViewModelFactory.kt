package com.miso.vinilo.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.miso.vinilo.data.adapter.NetworkConfig
import com.miso.vinilo.data.repository.AlbumRepository

class AlbumViewModelFactory(
    private val context: Context,
    private val baseUrl: String = NetworkConfig.baseUrl
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlbumViewModel::class.java)) {
            val repository = AlbumRepository.create(context, baseUrl)
            return AlbumViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}