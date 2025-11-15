package com.miso.vinilo.data.repository

import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterMusicians
import com.miso.vinilo.data.database.dao.MusicianDao
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicianRepositoryFactoryTest {

    @Test
    fun `create factory returns repository wired to adapter produced by NetworkServiceAdapterMusicians`() = runTest {
        // Prepare a fake adapter that returns a known success result
        val fakeData = listOf(
            MusicianDto(10, "Test Artist", "", "desc", "2000-01-01T00:00:00.000Z")
        )
        val fakeAdapter = mockk<NetworkServiceAdapterMusicians>()
        coEvery { fakeAdapter.getMusicians() } returns NetworkResult.Success(fakeData)

        // Provide a relaxed mock DAO (not used by this test path except for delete/insert in forceRefresh)
        val fakeDao = mockk<MusicianDao>(relaxed = true)
        coEvery { fakeDao.deleteAll() } returns Unit
        coEvery { fakeDao.insertAll(any()) } returns Unit

        // Mock the companion factory to return our fake adapter when create(baseUrl) is called
        mockkObject(NetworkServiceAdapterMusicians.Companion)
        every { NetworkServiceAdapterMusicians.create(any()) } returns fakeAdapter

        try {
            val repo = MusicianRepository.create("http://example/", fakeDao)
            // Invoke forceRefresh to trigger adapter usage
            repo.forceRefresh()

            // Verify the companion create was invoked and adapter.getMusicians delegated
            verify { NetworkServiceAdapterMusicians.create("http://example/") }
            io.mockk.coVerify { fakeAdapter.getMusicians() }
            // Ensure DAO persistence
            io.mockk.coVerify { fakeDao.deleteAll() }
            io.mockk.coVerify { fakeDao.insertAll(any()) }
            assertTrue(true) // basic assertion to satisfy JUnit when no exceptions
        } finally {
            // Clean up mocked object to avoid test pollution
            unmockkObject(NetworkServiceAdapterMusicians.Companion)
        }
    }
}
