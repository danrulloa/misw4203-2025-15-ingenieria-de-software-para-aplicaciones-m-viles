package com.miso.vinilo.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.miso.vinilo.domain.MusicianController
import com.miso.vinilo.data.model.Musician
import com.miso.vinilo.data.network.NetworkResult
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
    fun `loadMusicians updates state to Success when controller returns success`() = runTest {
        val expected = listOf(
            Musician(1, "Adele", "", "Singer", "1988-05-05T00:00:00.000Z")
        )
        val controller = mockk<MusicianController>()
        coEvery { controller.getMusicians() } returns NetworkResult.Success(expected)

        val vm = MusicianViewModel(controller)

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
    fun `loadMusicians updates state to Error when controller returns error`() = runTest {
        val controller = mockk<MusicianController>()
        coEvery { controller.getMusicians() } returns NetworkResult.Error("fail")

        val vm = MusicianViewModel(controller)

        vm.loadMusicians()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is MusicianViewModel.UiState.Error)
        val message = (state as MusicianViewModel.UiState.Error).message
        assertEquals("fail", message)
    }
}
