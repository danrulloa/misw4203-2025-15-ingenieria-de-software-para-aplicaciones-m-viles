package com.miso.vinilo.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.repository.AlbumRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAlbum updates state to Success when repository returns success`() = runTest {
        // Arrange
        val albumId = 100L
        val expectedAlbum = AlbumDto(albumId, "Test Album", "", "", "", "", "", emptyList(), emptyList())
        val mockRepo = mockk<AlbumRepository>()
        coEvery { mockRepo.getAlbum(albumId) } returns NetworkResult.Success(expectedAlbum)
        val viewModel = AlbumViewModel(mockRepo)

        // Act
        viewModel.loadAlbum(albumId)
        testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutine completes

        // Assert
        val state = viewModel.albumDetailState.value
        assertTrue(state is AlbumViewModel.AlbumDetailUiState.Success)
        val data = (state as AlbumViewModel.AlbumDetailUiState.Success).data
        assertEquals(expectedAlbum, data)
    }

    @Test
    fun `loadAlbum updates state to Error when repository returns error`() = runTest {
        // Arrange
        val albumId = 100L
        val errorMessage = "Network failed"
        val mockRepo = mockk<AlbumRepository>()
        coEvery { mockRepo.getAlbum(albumId) } returns NetworkResult.Error(errorMessage)
        val viewModel = AlbumViewModel(mockRepo)

        // Act
        viewModel.loadAlbum(albumId)
        testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutine completes

        // Assert
        val state = viewModel.albumDetailState.value
        assertTrue(state is AlbumViewModel.AlbumDetailUiState.Error)
        val message = (state as AlbumViewModel.AlbumDetailUiState.Error).message
        assertEquals(errorMessage, message)
    }
}
