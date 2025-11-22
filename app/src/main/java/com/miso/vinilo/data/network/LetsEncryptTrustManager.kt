package com.miso.vinilo.data.network

import android.content.Context
import com.miso.vinilo.R
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class LetsEncryptTrustManager(context: Context) : X509TrustManager {

    private val trustManagers: Array<X509TrustManager>

    init {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)

        // Load ISRG Root X1
        context.resources.openRawResource(R.raw.isrg_root_x1).use { inputStream ->
            val cert = certificateFactory.generateCertificate(inputStream) as X509Certificate
            keyStore.setCertificateEntry("isrg_root_x1", cert)
        }

        // Load ISRG Root X2
        context.resources.openRawResource(R.raw.isrg_root_x2).use { inputStream ->
            val cert = certificateFactory.generateCertificate(inputStream) as X509Certificate
            keyStore.setCertificateEntry("isrg_root_x2", cert)
        }

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        // Get the TrustManagers from the Factory
        val customTrustManagers = trustManagerFactory.trustManagers
        val systemTrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        systemTrustManagerFactory.init(null as KeyStore?)
        val systemTrustManagers = systemTrustManagerFactory.trustManagers

        // Combine them? For simplicity in this specific use case (fixing Wikimedia on old Android),
        // we will primarily rely on our custom one for the specific certs, but ideally we want a composite one.
        // However, standard OkHttp pattern is to provide the X509TrustManager that holds the anchors.
        
        // A better approach for "adding" to system trust is to create a KeyStore that *starts* with system trust
        // but that's hard on Android.
        
        // Instead, we'll implement checkServerTrusted to try our custom one, and if it fails, try the system one.
        // Or simpler: just use our custom one which ONLY has Let's Encrypt. 
        // BUT that would break other sites.
        
        // So we need a Composite TrustManager.
        
        val combined = ArrayList<X509TrustManager>()
        
        // Add our custom one
        for (tm in customTrustManagers) {
            if (tm is X509TrustManager) combined.add(tm)
        }
        
        // Add system one
        for (tm in systemTrustManagers) {
            if (tm is X509TrustManager) combined.add(tm)
        }
        
        trustManagers = combined.toTypedArray()
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        // We don't care about client auth for this app
        try {
            trustManagers.firstOrNull()?.checkClientTrusted(chain, authType)
        } catch (e: Exception) {
            // Try others? Client auth is usually server-side request.
            // For now, just delegate to first (likely our custom or system)
        }
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        // Try to validate with any of the trust managers
        var lastException: Exception? = null
        
        for (tm in trustManagers) {
            try {
                tm.checkServerTrusted(chain, authType)
                return // Success!
            } catch (e: Exception) {
                lastException = e
            }
        }
        
        // If we get here, none succeeded
        throw lastException ?: java.security.cert.CertificateException("No TrustManager trusted this chain")
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        val issuers = ArrayList<X509Certificate>()
        for (tm in trustManagers) {
            issuers.addAll(tm.acceptedIssuers)
        }
        return issuers.toTypedArray()
    }
}
