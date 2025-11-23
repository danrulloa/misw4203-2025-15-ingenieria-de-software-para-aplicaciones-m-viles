package com.miso.vinilo.ui.viewmodels

import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.dto.CollectorAlbumDto
import com.miso.vinilo.data.dto.CollectorDto
import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.dto.PerformerDto
import com.miso.vinilo.data.repository.AlbumRepository
import com.miso.vinilo.data.repository.CollectorRepository
import com.miso.vinilo.data.repository.MusicianRepository
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
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollectorDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var collectorRepository: CollectorRepository
    private lateinit var albumRepository: AlbumRepository
    private lateinit var musicianRepository: MusicianRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        collectorRepository = mockk()
        albumRepository = mockk()
        musicianRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCollectorDetail updates state to Success when repository returns success`() = runTest {
        val collectorAlbums = listOf(CollectorAlbumDto(1, 100, "Active"))
        val expectedCollector = CollectorDto(
            id = 1,
            name = "Manolo",
            telephone = "123",
            email = "test@test.com",
            collectorAlbums = collectorAlbums,
            comments = null,
            favoritePerformers = null
        )
        val albums = listOf(
            AlbumDto(1, "Album One", "cover.jpg", "2020-01-01", "Desc", "Rock", "Label", null, null)
        )

        coEvery { collectorRepository.getCollectorDetail(1) } returns NetworkResult.Success(expectedCollector)
        coEvery { albumRepository.getAlbums() } returns NetworkResult.Success(albums)

        val viewModel = CollectorDetailViewModel(collectorRepository, albumRepository, musicianRepository)
        viewModel.getCollectorDetail(1)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CollectorDetailViewModel.UiState.Success)
        val enrichedCollector = (state as CollectorDetailViewModel.UiState.Success).collector
        assertEquals("Manolo", enrichedCollector.name)
        assertEquals(1, enrichedCollector.collectorAlbums.size)
        assertEquals("Album One", enrichedCollector.collectorAlbums[0].name)
        assertEquals("cover.jpg", enrichedCollector.collectorAlbums[0].cover)
        assertEquals(100, enrichedCollector.collectorAlbums[0].price)
    }

    @Test
    fun `getCollectorDetail updates state to Error when repository returns error`() = runTest {
        coEvery { collectorRepository.getCollectorDetail(1) } returns NetworkResult.Error("Network error")
        coEvery { albumRepository.getAlbums() } returns NetworkResult.Success(emptyList())

        val viewModel = CollectorDetailViewModel(collectorRepository, albumRepository, musicianRepository)
        viewModel.getCollectorDetail(1)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CollectorDetailViewModel.UiState.Error)
        assertEquals("Network error", (state as CollectorDetailViewModel.UiState.Error).message)
    }

    @Test
    fun `getCollectorDetail enriches performer images from musician endpoint`() = runTest {
        val performers = listOf(
            PerformerDto(1, "Artist One", "", "Description", "1990-01-01")
        )
        val expectedCollector = CollectorDto(
            id = 1,
            name = "Manolo",
            telephone = "123",
            email = "test@test.com",
            collectorAlbums = null,
            comments = null,
            favoritePerformers = performers
        )
        val musician = MusicianDto(1, "Artist One", "artist.jpg", "Description", "1990-01-01")

        coEvery { collectorRepository.getCollectorDetail(1) } returns NetworkResult.Success(expectedCollector)
        coEvery { albumRepository.getAlbums() } returns NetworkResult.Success(emptyList())
        coEvery { musicianRepository.getMusicians() } returns NetworkResult.Success(listOf(musician))

        val viewModel = CollectorDetailViewModel(collectorRepository, albumRepository, musicianRepository)
        viewModel.getCollectorDetail(1)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CollectorDetailViewModel.UiState.Success)
        val enrichedCollector = (state as CollectorDetailViewModel.UiState.Success).collector
        assertEquals(1, enrichedCollector.favoritePerformers.size)
        assertEquals("artist.jpg", enrichedCollector.favoritePerformers[0].image)
    }

    @Test
    fun `getCollectorDetail handles missing album in album list`() = runTest {
        val collectorAlbums = listOf(CollectorAlbumDto(999, 50, "Active"))
        val expectedCollector = CollectorDto(
            id = 1,
            name = "Manolo",
            telephone = "123",
            email = "test@test.com",
            collectorAlbums = collectorAlbums,
            comments = null,
            favoritePerformers = null
        )

        coEvery { collectorRepository.getCollectorDetail(1) } returns NetworkResult.Success(expectedCollector)
        coEvery { albumRepository.getAlbums() } returns NetworkResult.Success(emptyList())

        val viewModel = CollectorDetailViewModel(collectorRepository, albumRepository, musicianRepository)
        viewModel.getCollectorDetail(1)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CollectorDetailViewModel.UiState.Success)
        val enrichedCollector = (state as CollectorDetailViewModel.UiState.Success).collector
        assertEquals(1, enrichedCollector.collectorAlbums.size)
        assertEquals("Álbum #999", enrichedCollector.collectorAlbums[0].name)
        assertEquals("", enrichedCollector.collectorAlbums[0].cover)
    }
}
