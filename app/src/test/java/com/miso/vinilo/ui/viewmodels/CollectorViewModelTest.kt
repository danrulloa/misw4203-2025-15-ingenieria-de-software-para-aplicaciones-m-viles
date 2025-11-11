package com.miso.vinilo.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.miso.vinilo.data.dto.CollectorDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.repository.CollectorRepository
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
class CollectorViewModelTest {

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
    fun `loadCollectors updates state to Success when repository returns success`() = runTest {
        val expected = listOf(
            CollectorDto(1, "Manolo Bellon", "3502457896", "manollo@caracol.com.co", null, null, null)
        )
        val repo = mockk<CollectorRepository>()
        coEvery { repo.getCollectors() } returns NetworkResult.Success(expected)

        val vm = CollectorViewModel(repo)

        // call the explicit load function introduced in the refactor
        vm.loadCollectors()

        // advance the dispatcher so the viewModelScope coroutine runs
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is CollectorViewModel.UiState.Success)
        val data = (state as CollectorViewModel.UiState.Success).data
        assertEquals(expected, data)
    }

    @Test
    fun `loadCollectors updates state to Error when repository returns error`() = runTest {
        val repo = mockk<CollectorRepository>()
        coEvery { repo.getCollectors() } returns NetworkResult.Error("fail")

        val vm = CollectorViewModel(repo)

        vm.loadCollectors()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is CollectorViewModel.UiState.Error)
        val message = (state as CollectorViewModel.UiState.Error).message
        assertEquals("fail", message)
    }
}