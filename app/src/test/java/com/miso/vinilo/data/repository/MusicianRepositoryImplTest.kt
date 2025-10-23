package com.miso.vinilo.data.repository

import com.miso.vinilo.data.model.Musician
import com.miso.vinilo.data.network.MusicianServiceAdapter
import com.miso.vinilo.data.network.NetworkResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicianRepositoryImplTest {

    @Test
    fun `getMusicians returns success when adapter returns success`() = runTest {
        val adapter = mockk<MusicianServiceAdapter>()
        val expected = listOf(
            Musician(1, "Adele Laurie Blue Adkins", "", "Singer", "1988-05-05T00:00:00.000Z")
        )
        coEvery { adapter.getMusicians() } returns NetworkResult.Success(expected)

        val repo = MusicianRepositoryImpl(adapter)
        val result = repo.getMusicians()

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals(expected, data)
    }

    @Test
    fun `getMusicians returns error when adapter returns error`() = runTest {
        val adapter = mockk<MusicianServiceAdapter>()
        coEvery { adapter.getMusicians() } returns NetworkResult.Error("network failure")

        val repo = MusicianRepositoryImpl(adapter)
        val result = repo.getMusicians()

        assertTrue(result is NetworkResult.Error)
        val message = (result as NetworkResult.Error).message
        assertEquals("network failure", message)
    }
}

