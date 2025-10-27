package com.miso.vinilo.data.repository

import com.miso.vinilo.data.model.Musician
import com.miso.vinilo.data.network.NetworkResult
import com.miso.vinilo.data.network.NetworkServiceAdapterMusicians
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicianRepositoryFactoryTest {

    @Test
    fun `create factory returns repository wired to adapter produced by NetworkServiceAdapterMusicians`() = runTest {
        // Prepare a fake adapter that returns a known success result
        val fakeData = listOf(
            Musician(10, "Test Artist", "", "desc", "2000-01-01T00:00:00.000Z")
        )
        val fakeAdapter = mockk<NetworkServiceAdapterMusicians>()
        coEvery { fakeAdapter.getMusicians() } returns NetworkResult.Success(fakeData)

        // Mock the companion factory to return our fake adapter when create(baseUrl) is called
        mockkObject(NetworkServiceAdapterMusicians.Companion)
        every { NetworkServiceAdapterMusicians.create(any()) } returns fakeAdapter

        try {
            val repo = MusicianRepositoryImpl.create("http://example/")
            // The created repo should delegate to our fake adapter
            val result = repo.getMusicians()

            assertTrue(result is NetworkResult.Success)
            val data = (result as NetworkResult.Success).data
            assertEquals(fakeData, data)

            // Verify the companion create was invoked
            verify { NetworkServiceAdapterMusicians.create("http://example/") }
        } finally {
            // Clean up mocked object to avoid test pollution
            unmockkObject(NetworkServiceAdapterMusicians.Companion)
        }
    }
}
