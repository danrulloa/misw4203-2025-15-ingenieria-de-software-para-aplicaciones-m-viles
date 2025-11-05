package com.miso.vinilo.data.repository

import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterAlbums
import com.miso.vinilo.data.dto.AlbumDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumRepositoryTest {

    @Test
    fun `getAlbum calls getAlbum on the service adapter and returns its result`() = runTest {
        // Arrange
        val albumId = 100L
        val expectedAlbum = AlbumDto(albumId, "Test Album", "", "", "", "", "", emptyList(), emptyList())
        val expectedResult = NetworkResult.Success(expectedAlbum)

        val mockServiceAdapter = mockk<NetworkServiceAdapterAlbums>()
        coEvery { mockServiceAdapter.getAlbum(albumId) } returns expectedResult

        val repository = AlbumRepository(mockServiceAdapter)

        // Act
        val result = repository.getAlbum(albumId)

        // Assert
        // Verify that the adapter's getAlbum function was called exactly once with the correct id
        coVerify(exactly = 1) { mockServiceAdapter.getAlbum(albumId) }

        // Verify that the repository returns the exact result from the adapter
        assertEquals(expectedResult, result)
    }
}
