package com.miso.vinilo.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.miso.vinilo.data.adapter.NetworkConfig
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.dto.CommentDto
import com.miso.vinilo.data.repository.AlbumRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
        mockRepo = mockk<AlbumRepository>(relaxed = true)
        viewModel = AlbumViewModel(mockRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAlbum updates state to Success when repository returns success`() = runTest {
        val albumId = 100L
        val expectedAlbum = AlbumDto(albumId, "Test Album", "", "", "", "", "", null, null, null)
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
    fun `postComment updates state to Success when repository call is successful`() = runTest {
        // Arrange
        val albumId = 100L
        val rating = 5
        val description = "Awesome album!"
        val expectedComment = CommentDto(id = 1, description = description, rating = rating)
        coEvery { mockRepo.postComment(eq(albumId), any()) } returns NetworkResult.Success(expectedComment)

        // Act
        viewModel.postComment(albumId, rating, description)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.postCommentState.value
        assertTrue(state is AlbumViewModel.PostCommentUiState.Success)
        assertEquals(expectedComment, (state as AlbumViewModel.PostCommentUiState.Success).data)
        coVerify { mockRepo.postComment(eq(albumId), any()) }
        coVerify { mockRepo.getAlbum(eq(albumId)) } // Verify that the album details are reloaded
    }

    @Test
    fun `postComment updates state to Error when repository call fails`() = runTest {
        // Arrange
        val albumId = 100L
        val rating = 5
        val description = "Awesome album!"
        val errorMessage = "Could not post comment"
        coEvery { mockRepo.postComment(eq(albumId), any()) } returns NetworkResult.Error(errorMessage)

        // Act
        viewModel.postComment(albumId, rating, description)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.postCommentState.value
        assertTrue(state is AlbumViewModel.PostCommentUiState.Error)
        assertEquals(errorMessage, (state as AlbumViewModel.PostCommentUiState.Error).message)
        coVerify { mockRepo.postComment(eq(albumId), any()) }
    }

    @Test
    fun `resetPostCommentState should set state back to Idle`() = runTest {
        // Arrange: First set a non-idle state
        coEvery { mockRepo.postComment(any(), any()) } returns NetworkResult.Success(mockk())
        viewModel.postComment(100L, 5, "Test")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.postCommentState.value is AlbumViewModel.PostCommentUiState.Success)

        // Act
        viewModel.resetPostCommentState()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.postCommentState.value
        assertTrue(state is AlbumViewModel.PostCommentUiState.Idle)
    }

    @Test
    fun `loadAlbums updates state to Success when repository returns success`() = runTest {
        val expectedAlbums = listOf(AlbumDto(100L, "Test Album", "", "", "", "", "", null, null, null))
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

}
