package com.miso.vinilo.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.miso.vinilo.Utils.MainDispatcherRule
import com.miso.vinilo.Utils.getOrAwaitValue
import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.repository.MusicianRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MusicianViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMusicians updates state to Success when repository returns success`() = runTest {
        val expected = listOf(
            MusicianDto(1, "Adele", "", "Singer", "1988-05-05T00:00:00.000Z")
        )
        val repo = mockk<MusicianRepository>()
        coEvery { repo.getMusicians() } returns NetworkResult.Success(expected)

        val vm = MusicianViewModel(repo)

        // call the explicit load function introduced in the refactor
        vm.loadMusicians()

        // advance the dispatcher so the viewModelScope coroutine runs
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is MusicianViewModel.UiState.Success)
        val data = (state as MusicianViewModel.UiState.Success).data
        assertEquals(expected, data)
    }

    @Test
    fun `loadMusicians updates state to Error when repository returns error`() = runTest {
        val repo = mockk<MusicianRepository>()
        coEvery { repo.getMusicians() } returns NetworkResult.Error("fail")

        val vm = MusicianViewModel(repo)

        vm.loadMusicians()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is MusicianViewModel.UiState.Error)
        val message = (state as MusicianViewModel.UiState.Error).message
        assertEquals("fail", message)
    }

    @Test
    fun `loadMusician success updates detailState with mapped albums`() = runTest {
        // Arrange
        val repo: MusicianRepository = mock()

        val albums = listOf(
            AlbumDto(
                id = 100,
                name = "Buscando América",
                cover = "https://example.com/cover.jpg",
                releaseDate = "1984-08-01T00:00:00.000Z",
                description = "desc",
                genre = "Salsa",
                recordLabel = "Elektra",
                tracks = emptyList(),
                performers = emptyList()
            )
        )

        val dto = MusicianDto(
            id = 100L,
            name = "Rubén Blades Bellido de Luna",
            image = "https://example.com/ruben.jpg",
            description = "Cantante",
            birthDate = "1948-07-16T00:00:00.000Z",
            albums = albums,
        )

        whenever(repo.getMusician(100L))
            .thenReturn(NetworkResult.Success(dto))

        val vm = MusicianViewModel(repo)

        // Act
        vm.loadMusician(100L)

        // 👇 deja que corran las corrutinas del viewModelScope (Main)
        advanceUntilIdle()

        // Assert
        val state = vm.detailState.getOrAwaitValue()
        assertTrue(state is MusicianViewModel.DetailUiState.Success)

        val data = (state as MusicianViewModel.DetailUiState.Success).data
        assertEquals("Rubén Blades Bellido de Luna", data.musician.name)
        assertEquals(1, data.albums.size)
        assertEquals("Buscando América", data.albums[0].name)
        assertEquals("1984", data.albums[0].year)
    }
}
