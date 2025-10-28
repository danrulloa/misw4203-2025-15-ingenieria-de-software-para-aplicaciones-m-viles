package com.miso.vinilo.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.repository.MusicianRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MusicianViewModelTest {

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
}
