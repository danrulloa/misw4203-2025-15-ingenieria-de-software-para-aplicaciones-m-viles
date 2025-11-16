package com.miso.vinilo.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import com.miso.vinilo.Utils.MainDispatcherRule
import com.miso.vinilo.Utils.getOrAwaitValue
import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.repository.MusicianRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MusicianViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        // ensure the test dispatcher is set as Main
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `init calls refreshIfNeeded on repository`() = runTest {
        val repo = mockk<MusicianRepository>(relaxed = true)
        coEvery { repo.getPagedMusicians() } returns flowOf(PagingData.empty())
        coJustRun { repo.refreshIfNeeded() }

        MusicianViewModel(repo)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.refreshIfNeeded() }
    }

    @Test
    fun `refreshMusicians sets isRefreshing to true then false`() = runTest {
        val repo = mockk<MusicianRepository>(relaxed = true)
        coEvery { repo.getPagedMusicians() } returns flowOf(PagingData.empty())
        // Simulate a bit of work so we can observe the intermediate refreshing state
        coEvery { repo.forceRefresh() } coAnswers {
            delay(10)
        }

        val vm = MusicianViewModel(repo)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.isRefreshing.value)

        vm.refreshMusicians()

        // Let the coroutine start and set isRefreshing = true, but do not complete the delay yet
        testDispatcher.scheduler.runCurrent()
        assertTrue(vm.isRefreshing.value)

        // Now advance to complete the refresh
        testDispatcher.scheduler.advanceUntilIdle()

        // Refreshing complete
        assertFalse(vm.isRefreshing.value)
        coVerify { repo.forceRefresh() }
    }

    @Test
    fun `refreshMusicians calls forceRefresh on repository`() = runTest {
        val repo = mockk<MusicianRepository>(relaxed = true)
        coEvery { repo.getPagedMusicians() } returns flowOf(PagingData.empty())
        coJustRun { repo.forceRefresh() }

        val vm = MusicianViewModel(repo)

        vm.refreshMusicians()

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.forceRefresh() }
    }

    @Test
    fun `musicians flow is provided by repository`() = runTest {
        val repo = mockk<MusicianRepository>(relaxed = true)
        val pagingData = PagingData.from(listOf(
            MusicianDto(1, "Adele", "", "Singer", "1988-05-05T00:00:00.000Z")
        ))
        coEvery { repo.getPagedMusicians() } returns flowOf(pagingData)

        val vm = MusicianViewModel(repo)
        assertNotNull(vm)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.getPagedMusicians() }
    }

    @Test
    fun `loadMusician success updates detailState with mapped albums`() = runTest {
        // Arrange
        val repo = mockk<MusicianRepository>()
        // Asegurar flujo vacío para el pager y evitar llamadas reales
        coEvery { repo.getPagedMusicians() } returns flowOf(PagingData.empty())
        // Evitar fallo por refresh en init
        coJustRun { repo.refreshIfNeeded() }

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

        coEvery { repo.getMusician(100L) } returns NetworkResult.Success(dto)

        val vm = MusicianViewModel(repo)

        // Act
        vm.loadMusician(100L)

        // permite que corran las corrutinas del viewModelScope
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
