package com.miso.vinilo.data.adapter

import com.miso.vinilo.data.adapter.retrofit.MusicianApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class NetworkServiceAdapterMusiciansUnitTest {

    private val api = mockk<MusicianApi>()
    private lateinit var adapter: NetworkServiceAdapterMusicians

    @Before
    fun setUp() {
        adapter = NetworkServiceAdapterMusicians(api)
    }

    @Test
    fun `getMusicians returns error when api throws IOException`() = runTest {
        coEvery { api.getMusicians() } throws IOException("network failure")

        val result = adapter.getMusicians()

        assertTrue(result is NetworkResult.Error)
        val err = result as NetworkResult.Error
        assertTrue(err.throwable is IOException)
        // message may be the localized message, ensure it's not null
        assertTrue(err.message.isNotBlank())
    }
}
