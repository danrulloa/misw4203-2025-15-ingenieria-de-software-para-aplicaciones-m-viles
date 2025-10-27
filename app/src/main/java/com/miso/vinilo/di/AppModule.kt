package com.miso.vinilo.di

import com.miso.vinilo.BuildConfig
import com.miso.vinilo.data.network.MusicianServiceAdapter
import com.miso.vinilo.data.network.NetworkServiceAdapterMusicians
import com.miso.vinilo.data.repository.MusicianRepository
import com.miso.vinilo.data.repository.MusicianRepositoryImpl
import com.miso.vinilo.domain.MusicianUseCase
import com.miso.vinilo.domain.MusicianUseCaseImpl
import com.miso.vinilo.viewmodels.MusicianViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Koin module providing network, repository, controller and ViewModel bindings.
val appModule = module {
    single<MusicianServiceAdapter> { NetworkServiceAdapterMusicians.create(BuildConfig.BASE_URL) }
    // Specify generic types explicitly to help the Kotlin compiler infer dependencies
    single<MusicianRepository> { MusicianRepositoryImpl(get<MusicianServiceAdapter>()) }
    single<MusicianUseCase> { MusicianUseCaseImpl(get<MusicianRepository>()) }
    viewModel { MusicianViewModel(get<MusicianUseCase>()) }
}
