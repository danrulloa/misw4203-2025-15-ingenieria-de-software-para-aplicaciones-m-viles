package com.miso.vinilo.domain

import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.repository.MusicianRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicianUseCaseTest {

    @Test
    fun `getMusicians returns success when repository returns success`() = runTest {
        val repo = mockk<MusicianRepository>()
        val expected = listOf(
            MusicianDto(1, "Adele Laurie Blue Adkins", "", "Singer", "1988-05-05T00:00:00.000Z")
        )
        coEvery { repo.getMusicians() } returns NetworkResult.Success(expected)

        val controller = MusicianUseCaseImpl(repo)
        val result = controller.getMusicians()

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals(expected, data)
    }

    @Test
    fun `getMusicians returns error when repository returns error`() = runTest {
        val repo = mockk<MusicianRepository>()
        coEvery { repo.getMusicians() } returns NetworkResult.Error("repo failure")

        val controller = MusicianUseCaseImpl(repo)
        val result = controller.getMusicians()

        assertTrue(result is NetworkResult.Error)
        val message = (result as NetworkResult.Error).message
        assertEquals("repo failure", message)
    }

    @Test
    fun `getMusicians returns empty list when repository returns empty success`() = runTest {
        val repo = mockk<MusicianRepository>()
        coEvery { repo.getMusicians() } returns NetworkResult.Success(emptyList())

        val controller = MusicianUseCaseImpl(repo)
        val result = controller.getMusicians()

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertTrue(data.isEmpty())
    }

    @Test
    fun `getMusicians returns multiple musicians when repository returns multiple`() = runTest {
        val repo = mockk<MusicianRepository>()
        val expected = listOf(
            MusicianDto(1, "Adele", "", "Singer", "1988-05-05T00:00:00.000Z"),
            MusicianDto(2, "Freddie Mercury", "", "Singer", "1946-09-05T00:00:00.000Z")
        )
        coEvery { repo.getMusicians() } returns NetworkResult.Success(expected)

        val controller = MusicianUseCaseImpl(repo)
        val result = controller.getMusicians()

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals(2, data.size)
        assertEquals(expected, data)
    }

    @Test
    fun `getMusicians returns error with throwable when repository provides throwable`() = runTest {
        val repo = mockk<MusicianRepository>()
        val cause = RuntimeException("boom")
        coEvery { repo.getMusicians() } returns NetworkResult.Error("failure", cause)

        val controller = MusicianUseCaseImpl(repo)
        val result = controller.getMusicians()

        assertTrue(result is NetworkResult.Error)
        val error = result as NetworkResult.Error
        assertEquals("failure", error.message)
        assertEquals(cause, error.throwable)
    }

    @Test
    fun `getMusicians delegates to repository exactly once`() = runTest {
        val repo = mockk<MusicianRepository>()
        coEvery { repo.getMusicians() } returns NetworkResult.Success(emptyList())

        val controller = MusicianUseCaseImpl(repo)
        controller.getMusicians()

        coVerify(exactly = 1) { repo.getMusicians() }
    }
}
