package com.miso.vinilo.di

import com.miso.vinilo.data.adapter.NetworkServiceAdapterMusicians
import com.miso.vinilo.data.adapter.NetworkServiceAdapterCollectors
import com.miso.vinilo.data.database.ViniloDatabase
import com.miso.vinilo.data.repository.MusicianRepository
import com.miso.vinilo.data.repository.CollectorRepository
import com.miso.vinilo.ui.viewmodels.MusicianViewModel
import com.miso.vinilo.ui.viewmodels.CollectorViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.miso.vinilo.data.adapter.NetworkConfig // Added to honor test overrides

// Koin module providing network, repository and ViewModel bindings.
val appModule = module {
    // Room Database
    single { ViniloDatabase.getDatabase(androidContext()) }
    single { get<ViniloDatabase>().musicianDao() }
    single { get<ViniloDatabase>().collectorDao() }

    // Musicians
    // Bind the concrete network adapter directly using NetworkConfig.baseUrl so tests can override it
    single { NetworkServiceAdapterMusicians.create(NetworkConfig.baseUrl) }
    // Provide the concrete repository instance with Room DAO
    single { MusicianRepository(get<NetworkServiceAdapterMusicians>(), get()) }
    // Provide ViewModel wired directly to the repository (use-case layer removed)
    viewModel { MusicianViewModel(get<MusicianRepository>()) }

    // Collectors
    // Bind the concrete network adapter directly
    single { NetworkServiceAdapterCollectors.create(NetworkConfig.baseUrl) }
    // Provide the concrete repository instance
    single { CollectorRepository(get<NetworkServiceAdapterCollectors>(), get()) }
    // Provide ViewModel wired directly to the repository (use-case layer removed)
    viewModel { CollectorViewModel(get<CollectorRepository>()) }
}
