package com.miso.vinilo.data.adapter

import com.miso.vinilo.BuildConfig

/**
 * Global network configuration used by repositories and adapters.
 * This allows tests to override the base URL at runtime (MockWebServer).
 */
object NetworkConfig {
    // Default to the compile-time BuildConfig value but allow tests to change it.
    @JvmStatic
    var baseUrl: String = BuildConfig.BASE_URL
}

