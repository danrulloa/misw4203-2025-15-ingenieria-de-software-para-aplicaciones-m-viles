package com.miso.vinilo

import android.app.Application
import com.miso.vinilo.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            // lightweight logger for development
            logger(PrintLogger(Level.INFO))
            modules(appModule)
        }
    }
}

