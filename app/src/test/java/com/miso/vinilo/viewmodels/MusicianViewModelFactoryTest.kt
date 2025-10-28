package com.miso.vinilo.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModel
import com.miso.vinilo.data.repository.MusicianRepository
import com.miso.vinilo.data.adapter.NetworkResult
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
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MusicianViewModelFactoryTest {

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
    fun `create returns MusicianViewModel when given MusicianViewModel class`() = runTest {
        val repo = mockk<MusicianRepository>(relaxed = true)
        // Ensure any suspend call used by init/fetchMusicians is handled
        coEvery { repo.getMusicians() } returns NetworkResult.Success(emptyList())

        val factory = MusicianViewModelFactory(repo)
        val vm = factory.create(MusicianViewModel::class.java)

        assertEquals(MusicianViewModel::class.java, vm::class.java)
    }

    @Test
    fun `create throws for unknown ViewModel class`() {
        val repo = mockk<MusicianRepository>(relaxed = true)
        val factory = MusicianViewModelFactory(repo)

        // Use a ViewModel subclass that is not MusicianViewModel to satisfy the generic bound
        class OtherViewModel : ViewModel()

        assertThrows(IllegalArgumentException::class.java) {
            factory.create(OtherViewModel::class.java)
        }
    }
}
