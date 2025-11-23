package com.miso.vinilo.di

import com.miso.vinilo.data.adapter.NetworkConfig
import com.miso.vinilo.data.adapter.NetworkServiceAdapterAlbums
import com.miso.vinilo.data.adapter.NetworkServiceAdapterCollectors
import com.miso.vinilo.data.adapter.NetworkServiceAdapterMusicians
import com.miso.vinilo.data.adapter.retrofit.AlbumApi
import com.miso.vinilo.data.adapter.retrofit.CollectorApi
import com.miso.vinilo.data.adapter.retrofit.MusicianApi
import com.miso.vinilo.data.database.ViniloDatabase
import com.miso.vinilo.data.repository.AlbumRepository
import com.miso.vinilo.data.repository.CollectorRepository
import com.miso.vinilo.data.repository.MusicianRepository
import com.miso.vinilo.ui.viewmodels.CollectorDetailViewModel
import com.miso.vinilo.ui.viewmodels.CollectorViewModel
import com.miso.vinilo.ui.viewmodels.MusicianViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val appModule = module {

    // General networking
    single {
        Retrofit.Builder()
            .baseUrl(NetworkConfig.baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }
    single { Dispatchers.IO }

    // Room Database
    single { ViniloDatabase.getDatabase(androidContext()) }

    // Albums
    single { get<Retrofit>().create(AlbumApi::class.java) }
    single { NetworkServiceAdapterAlbums(get(), get()) }
    single { AlbumRepository(get()) }

    // Musicians
    single { get<Retrofit>().create(MusicianApi::class.java) }
    single { get<ViniloDatabase>().musicianDao() }
    single { NetworkServiceAdapterMusicians(get(), get()) }
    single { MusicianRepository(get(), get()) }
    viewModel { MusicianViewModel(get()) }

    // Collectors
    single { get<Retrofit>().create(CollectorApi::class.java) }
    single { get<ViniloDatabase>().collectorDao() }
    single { NetworkServiceAdapterCollectors(get(), get()) }
    single { CollectorRepository(get(), get()) }
    // ViewModel for the list (uses Room)
    viewModel { CollectorViewModel(get()) }
    // ViewModel for the detail screen
    viewModel { CollectorDetailViewModel(get(), get(), get()) }
}
