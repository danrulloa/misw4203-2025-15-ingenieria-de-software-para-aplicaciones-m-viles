package com.miso.vinilo.data.adapter

import com.miso.vinilo.data.adapter.retrofit.MusicianApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkServiceAdapterMusiciansUnitTest {

    private val api = mockk<MusicianApi>()

    @Before
    fun setUp() {
        // no-op: adapter will be created inside each runTest with a TestDispatcher
    }

    @Test
    fun `getMusicians returns error when api throws IOException`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val adapter = NetworkServiceAdapterMusicians(api, testDispatcher)

        coEvery { api.getMusicians() } throws IOException("network failure")

        val result = adapter.getMusicians()

        assertTrue(result is NetworkResult.Error)
        val err = result as NetworkResult.Error
        assertTrue(err.throwable is IOException)
        // message may be the localized message, ensure it's not null
        assertTrue(err.message.isNotBlank())
    }
}
