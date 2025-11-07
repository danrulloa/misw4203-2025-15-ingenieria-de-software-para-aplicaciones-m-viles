package com.miso.vinilo.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.miso.vinilo.data.adapter.NetworkConfig
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockRepo: AlbumRepository
    private lateinit var viewModel: AlbumViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepo = mockk<AlbumRepository>()
        viewModel = AlbumViewModel(mockRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAlbum updates state to Success when repository returns success`() = runTest {
        val albumId = 100L
        val expectedAlbum = AlbumDto(albumId, "Test Album", "", "", "", "", "", null, null)
        coEvery { mockRepo.getAlbum(albumId) } returns NetworkResult.Success(expectedAlbum)

        viewModel.loadAlbum(albumId)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.albumDetailState.value
        assertTrue(state is AlbumViewModel.AlbumDetailUiState.Success)
        assertEquals(expectedAlbum, (state as AlbumViewModel.AlbumDetailUiState.Success).data)
    }

    @Test
    fun `loadAlbum updates state to Error when repository returns error`() = runTest {
        val albumId = 100L
        val errorMessage = "Network failed"
        coEvery { mockRepo.getAlbum(albumId) } returns NetworkResult.Error(errorMessage)

        viewModel.loadAlbum(albumId)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.albumDetailState.value
        assertTrue(state is AlbumViewModel.AlbumDetailUiState.Error)
        assertEquals(errorMessage, (state as AlbumViewModel.AlbumDetailUiState.Error).message)
    }

    @Test
    fun `loadAlbums updates state to Success when repository returns success`() = runTest {
        val expectedAlbums = listOf(AlbumDto(100L, "Test Album", "", "", "", "", "", null, null))
        coEvery { mockRepo.getAlbums() } returns NetworkResult.Success(expectedAlbums)

        viewModel.loadAlbums()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is AlbumViewModel.UiState.Success)
        assertEquals(expectedAlbums, (state as AlbumViewModel.UiState.Success).data)
    }

    @Test
    fun `loadAlbums updates state to Error when repository returns error`() = runTest {
        val errorMessage = "Network failed"
        coEvery { mockRepo.getAlbums() } returns NetworkResult.Error(errorMessage)

        viewModel.loadAlbums()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is AlbumViewModel.UiState.Error)
        assertEquals(errorMessage, (state as AlbumViewModel.UiState.Error).message)
    }

    @Test
    fun `constructor with baseUrl creates a valid ViewModel`() {
        // This test covers the secondary constructor that takes a baseUrl
        val viewModel = AlbumViewModel("http://localhost:3000/")
        assertNotNull(viewModel)
    }

    @Test
    fun `no-arg constructor creates a valid ViewModel`() {
        // This test covers the no-argument constructor
        // It relies on the global NetworkConfig, which is fine for a smoke test.
        val viewModel = AlbumViewModel()
        assertNotNull(viewModel)
    }
}
