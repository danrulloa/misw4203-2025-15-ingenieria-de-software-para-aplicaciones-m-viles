package com.miso.vinilo.di

import com.miso.vinilo.BuildConfig
import com.miso.vinilo.data.adapter.NetworkServiceAdapterMusicians
import com.miso.vinilo.data.adapter.NetworkServiceAdapterCollectors
import com.miso.vinilo.data.repository.MusicianRepository
import com.miso.vinilo.data.repository.CollectorRepository
import com.miso.vinilo.ui.viewmodels.MusicianViewModel
import com.miso.vinilo.ui.viewmodels.CollectorViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// Koin module providing network, repository and ViewModel bindings.
val appModule = module {
    // Musicians
    // Bind the concrete network adapter directly
    single { NetworkServiceAdapterMusicians.create(BuildConfig.BASE_URL) }
    // Provide the concrete repository instance
    single { MusicianRepository(get<NetworkServiceAdapterMusicians>()) }
    // Provide ViewModel wired directly to the repository (use-case layer removed)
    viewModel { MusicianViewModel(get<MusicianRepository>()) }

    // Collectors
    // Bind the concrete network adapter directly
    single { NetworkServiceAdapterCollectors.create(BuildConfig.BASE_URL) }
    // Provide the concrete repository instance
    single { CollectorRepository(get<NetworkServiceAdapterCollectors>()) }
    // Provide ViewModel wired directly to the repository (use-case layer removed)
    viewModel { CollectorViewModel(get<CollectorRepository>()) }
}
