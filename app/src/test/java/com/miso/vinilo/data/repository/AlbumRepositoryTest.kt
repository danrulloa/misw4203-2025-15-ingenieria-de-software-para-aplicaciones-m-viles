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
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumRepositoryTest {

    @Test
    fun `getAlbum calls getAlbum on the service adapter and returns its result`() = runTest {
        // Arrange
        val albumId = 100L
        val expectedAlbum = AlbumDto(albumId, "Test Album", "", "", "", "", "", null, null)
        val expectedResult = NetworkResult.Success(expectedAlbum)

        val mockServiceAdapter = mockk<NetworkServiceAdapterAlbums>()
        coEvery { mockServiceAdapter.getAlbum(albumId) } returns expectedResult

        val repository = AlbumRepository(mockServiceAdapter)

        // Act
        val result = repository.getAlbum(albumId)

        // Assert
        coVerify(exactly = 1) { mockServiceAdapter.getAlbum(albumId) }
        assertEquals(expectedResult, result)
    }

    // --- New test to increase coverage ---

    @Test
    fun `getAlbums calls getAlbums on the service adapter and returns its result`() = runTest {
        // Arrange
        val expectedAlbums = listOf(AlbumDto(100L, "Test Album", "", "", "", "", "", null, null))
        val expectedResult = NetworkResult.Success(expectedAlbums)

        val mockServiceAdapter = mockk<NetworkServiceAdapterAlbums>()
        coEvery { mockServiceAdapter.getAlbums() } returns expectedResult

        val repository = AlbumRepository(mockServiceAdapter)

        // Act
        val result = repository.getAlbums()

        // Assert
        coVerify(exactly = 1) { mockServiceAdapter.getAlbums() }
        assertEquals(expectedResult, result)
    }

    @Test
    fun `create factory returns a valid AlbumRepository instance`() {
        // Arrange
        val baseUrl = "http://localhost:3000/"

        // Act
        val repository = AlbumRepository.create(baseUrl)

        // Assert
        assertNotNull(repository)
    }
}
