package com.miso.vinilo.data.adapter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.miso.vinilo.data.adapter.retrofit.AlbumApi
import com.miso.vinilo.data.dto.AlbumDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkServiceAdapterAlbumsUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val api = mockk<AlbumApi>()
    private lateinit var adapter: NetworkServiceAdapterAlbums

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        adapter = NetworkServiceAdapterAlbums(api)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAlbums returns error when api throws IOException`() = runTest {
        coEvery { api.getAlbums() } throws IOException("network failure")

        val result = adapter.getAlbums()

        assertTrue(result is NetworkResult.Error)
        val err = result as NetworkResult.Error
        assertTrue(err.throwable is IOException)
        assertTrue(err.message.isNotBlank())
    }

    @Test
    fun `getAlbum returns success when api returns data`() = runTest {
        // Arrange
        val albumId = 1L
        val expectedDto = AlbumDto(albumId, "Test Album", "", "", "", "", "", emptyList(), emptyList())
        coEvery { api.getAlbum(albumId) } returns expectedDto

        // Act
        val result = adapter.getAlbum(albumId)

        // Assert
        assertTrue(result is NetworkResult.Success)
        val successResult = result as NetworkResult.Success
        assertEquals(expectedDto, successResult.data)
    }

    @Test
    fun `getAlbum returns error when api throws IOException`() = runTest {
        // Arrange
        val albumId = 1L
        coEvery { api.getAlbum(albumId) } throws IOException("network failure")

        // Act
        val result = adapter.getAlbum(albumId)

        // Assert
        assertTrue(result is NetworkResult.Error)
        val err = result as NetworkResult.Error
        assertTrue(err.throwable is IOException)
        assertTrue(err.message.isNotBlank())
    }
}
