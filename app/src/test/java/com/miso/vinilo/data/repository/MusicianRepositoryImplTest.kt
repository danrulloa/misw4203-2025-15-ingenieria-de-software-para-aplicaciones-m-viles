package com.miso.vinilo.data.repository

import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterMusicians
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MusicianRepositoryImplTest {

    private val serviceAdapter: NetworkServiceAdapterMusicians = mock()
    private val repository = MusicianRepository(serviceAdapter)

    @Test
    fun `getMusicians returns success when adapter returns success`() = runTest {
        val adapter = mockk<NetworkServiceAdapterMusicians>()
        val expected = listOf(
            MusicianDto(1, "Adele Laurie Blue Adkins", "", "Singer", "1988-05-05T00:00:00.000Z")
        )
        coEvery { adapter.getMusicians() } returns NetworkResult.Success(expected)

        val repo = MusicianRepository(adapter)
        val result = repo.getMusicians()

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals(expected, data)
    }

    @Test
    fun `getMusicians returns error when adapter returns error`() = runTest {
        val adapter = mockk<NetworkServiceAdapterMusicians>()
        coEvery { adapter.getMusicians() } returns NetworkResult.Error("network failure")

        val repo = MusicianRepository(adapter)
        val result = repo.getMusicians()

        assertTrue(result is NetworkResult.Error)
        val message = (result as NetworkResult.Error).message
        assertEquals("network failure", message)
    }

    @Test
    fun `getMusician delegates call to serviceAdapter and returns NetworkResult`() = runTest {
        // Arrange
        val dto = MusicianDto(
            id = 100L,
            name = "Rubén Blades Bellido de Luna",
            image = "url",
            description = "desc",
            birthDate = "1948-07-16T00:00:00.000Z",
            albums = emptyList(),
        )

        whenever(serviceAdapter.getMusician(100L))
            .thenReturn(NetworkResult.Success(dto))

        // Act
        val result = repository.getMusician(100L)

        // Assert
        verify(serviceAdapter).getMusician(100L)

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals("Rubén Blades Bellido de Luna", data.name)
    }



}
