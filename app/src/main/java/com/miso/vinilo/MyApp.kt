package com.miso.vinilo

import android.app.Application
import com.miso.vinilo.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import coil.ImageLoader
import coil.ImageLoaderFactory
import okhttp3.OkHttpClient
import javax.net.ssl.SSLContext
import org.koin.core.logger.PrintLogger

class MyApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApp)
            logger(PrintLogger(Level.INFO))
            modules(appModule)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                val clientBuilder = OkHttpClient.Builder()

                // Only apply custom trust manager for older Android versions (API < 26)
                // Android 8.0+ (API 26) and above have updated root certificates and strict security.
                if (android.os.Build.VERSION.SDK_INT < 26) {
                    clientBuilder.connectionSpecs(listOf(okhttp3.ConnectionSpec.MODERN_TLS, okhttp3.ConnectionSpec.COMPATIBLE_TLS))
                    try {
                        // Use our custom TrustManager that includes Let's Encrypt roots
                        val trustManager = com.miso.vinilo.data.network.LetsEncryptTrustManager(this)
                        
                        val sslContext = SSLContext.getInstance("TLS")
                        sslContext.init(null, arrayOf(trustManager), null)
                        
                        val sslSocketFactory = sslContext.socketFactory
                        
                        clientBuilder.sslSocketFactory(sslSocketFactory, trustManager)
                        android.util.Log.d("MyApp", "LetsEncryptTrustManager configured successfully for Coil (Legacy)")
                    } catch (e: Exception) {
                        android.util.Log.e("MyApp", "Failed to configure LetsEncryptTrustManager", e)
                    }
                }

                clientBuilder.build()
            }
            .build()
    }
}
