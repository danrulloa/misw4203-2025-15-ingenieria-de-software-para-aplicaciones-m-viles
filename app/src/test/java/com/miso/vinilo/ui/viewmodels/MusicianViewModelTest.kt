package com.miso.vinilo.ui.viewmodels

import androidx.paging.PagingData
import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.repository.MusicianRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.coJustRun
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MusicianViewModelTest {

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

    @Suppress("UNUSED_EXPRESSION")
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
}
