package com.miso.vinilo.data.network

import com.miso.vinilo.data.network.retrofit.AlbumApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class NetworkServiceAdapterAlbumsUnitTest {

    private val api = mockk<AlbumApi>()
    private lateinit var adapter: AlbumServiceAdapter

    @Before
    fun setUp() {
        adapter = NetworkServiceAdapterAlbums(api)
    }

    @Test
    fun `getAlbums returns error when api throws IOException`() = runTest {
        coEvery { api.getAlbums() } throws IOException("network failure")

        val result = adapter.getAlbums()

        assertTrue(result is NetworkResult.Error)
        val err = result as NetworkResult.Error
        assertTrue(err.throwable is IOException)
        // message may be the localized message, ensure it's not null
        assertTrue(err.message.isNotBlank())
    }
}