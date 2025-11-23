package com.miso.vinilo.data.repository

import com.miso.vinilo.data.dto.CollectorDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterCollectors
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectorRepositoryImplTest {

    @Test
    fun `getCollectors returns success when adapter returns success`() = runTest {
        val adapter = mockk<NetworkServiceAdapterCollectors>()
        val expected = listOf(
            CollectorDto(1, "Manolo Bellon", "3502457896", "manollo@caracol.com.co", null, null, null)
        )
        coEvery { adapter.getCollectors() } returns NetworkResult.Success(expected)

        val repo = CollectorRepository(adapter)
        val result = repo.getCollectors()

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals(expected, data)
    }

    @Test
    fun `getCollectors returns error when adapter returns error`() = runTest {
        val adapter = mockk<NetworkServiceAdapterCollectors>()
        coEvery { adapter.getCollectors() } returns NetworkResult.Error("network failure")

        val repo = CollectorRepository(adapter)
        val result = repo.getCollectors()

        assertTrue(result is NetworkResult.Error)
        val message = (result as NetworkResult.Error).message
        assertEquals("network failure", message)
    }

    @Test
    fun `getCollectorDetail returns success when adapter returns success`() = runTest {
        val adapter = mockk<NetworkServiceAdapterCollectors>()
        val expected = CollectorDto(1, "Manolo Bellon", "3502457896", "manollo@caracol.com.co", null, null, null)
        coEvery { adapter.getCollectorDetail(1) } returns NetworkResult.Success(expected)

        val repo = CollectorRepository(adapter)
        val result = repo.getCollectorDetail(1)

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals(expected, data)
    }

    @Test
    fun `getCollectorDetail returns error when adapter returns error`() = runTest {
        val adapter = mockk<NetworkServiceAdapterCollectors>()
        coEvery { adapter.getCollectorDetail(1) } returns NetworkResult.Error("network failure")

        val repo = CollectorRepository(adapter)
        val result = repo.getCollectorDetail(1)

        assertTrue(result is NetworkResult.Error)
        val message = (result as NetworkResult.Error).message
        assertEquals("network failure", message)
    }
}