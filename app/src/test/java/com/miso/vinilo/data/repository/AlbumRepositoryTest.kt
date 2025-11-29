package com.miso.vinilo.data.repository

import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterAlbums
import com.miso.vinilo.data.database.dao.AlbumDao
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.dto.CollectorIdDto
import com.miso.vinilo.data.dto.CommentDto
import com.miso.vinilo.data.dto.NewCommentDto
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
        val expectedAlbum = AlbumDto(albumId, "Test Album", "", "", "", "", "", null, null, null)
        val expectedResult = NetworkResult.Success(expectedAlbum)
        val mockAlbumDao = mockk<AlbumDao>(relaxed = true)
        val mockServiceAdapter = mockk<NetworkServiceAdapterAlbums>()
        coEvery { mockServiceAdapter.getAlbum(albumId) } returns expectedResult

        val repository = AlbumRepository(mockServiceAdapter, mockAlbumDao)

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
        val expectedAlbums = listOf(AlbumDto(100L, "Test Album", "", "", "", "", "", null, null, null))
        val expectedResult = NetworkResult.Success(expectedAlbums)
        val mockAlbumDao = mockk<AlbumDao>(relaxed = true)
        val mockServiceAdapter = mockk<NetworkServiceAdapterAlbums>()
        coEvery { mockServiceAdapter.getAlbums() } returns expectedResult

        val repository = AlbumRepository(mockServiceAdapter, mockAlbumDao)

        // Act
        val result = repository.getAlbums()

        // Assert
        coVerify(exactly = 1) { mockServiceAdapter.getAlbums() }
        assertEquals(expectedResult, result)
    }

    @Test
    fun `postComment calls postComment on the service adapter and returns its result`() = runTest {
        // Arrange
        val albumId = 100L
        val newComment = NewCommentDto(rating = 5, description = "A comment", collector = CollectorIdDto(100L))
        val expectedComment = CommentDto(id = 1, description = "A comment", rating = 5)
        val expectedResult = NetworkResult.Success(expectedComment)
        val mockAlbumDao = mockk<AlbumDao>()
        val mockServiceAdapter = mockk<NetworkServiceAdapterAlbums>()
        coEvery { mockServiceAdapter.postComment(albumId, newComment) } returns expectedResult

        val repository = AlbumRepository(mockServiceAdapter, mockAlbumDao)

        // Act
        val result = repository.postComment(albumId, newComment)

        // Assert
        coVerify(exactly = 1) { mockServiceAdapter.postComment(albumId, newComment) }
        assertEquals(expectedResult, result)
    }

}
