package com.miso.vinilo.data.adapter

import com.miso.vinilo.data.adapter.retrofit.CollectorApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class NetworkServiceAdapterCollectorsUnitTest {

    private val api = mockk<CollectorApi>()
    private lateinit var adapter: NetworkServiceAdapterCollectors

    @Before
    fun setUp() {
        adapter = NetworkServiceAdapterCollectors(api)
    }

    @Test
    fun `getCollectors returns error when api throws IOException`() = runTest {
        coEvery { api.getCollectors() } throws IOException("network failure")

        val result = adapter.getCollectors()

        assertTrue(result is NetworkResult.Error)
        val err = result as NetworkResult.Error
        assertTrue(err.throwable is IOException)
        // message may be the localized message, ensure it's not null
        assertTrue(err.message.isNotBlank())
    }
}