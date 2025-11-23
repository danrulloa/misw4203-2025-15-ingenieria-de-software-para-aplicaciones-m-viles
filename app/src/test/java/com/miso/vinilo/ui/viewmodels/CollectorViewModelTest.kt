package com.miso.vinilo.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.miso.vinilo.data.repository.CollectorRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
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
    fun `onScreenReady calls repository to refresh if needed`() = runTest {
        // Arrange
        val repository = mockk<CollectorRepository>(relaxUnitFun = true)
        every { repository.getPagedCollectors() } returns emptyFlow()
        val viewModel = CollectorViewModel(repository)

        // Act
        viewModel.onScreenReady()
        testDispatcher.scheduler.advanceUntilIdle() // Run the coroutine

        // Assert
        coVerify { repository.refreshIfNeeded() }
    }

    @Test
    fun `onForceRefresh calls repository to force a refresh`() = runTest {
        // Arrange
        val repository = mockk<CollectorRepository>(relaxUnitFun = true)
        every { repository.getPagedCollectors() } returns emptyFlow()
        val viewModel = CollectorViewModel(repository)

        // Act
        viewModel.onForceRefresh()
        testDispatcher.scheduler.advanceUntilIdle() // Run the coroutine

        // Assert
        coVerify { repository.forceRefresh() }
    }
}
