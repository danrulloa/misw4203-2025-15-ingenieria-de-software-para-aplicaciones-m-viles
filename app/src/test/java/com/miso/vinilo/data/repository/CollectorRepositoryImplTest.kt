package com.miso.vinilo.data.repository

import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterCollectors
import com.miso.vinilo.data.database.dao.CollectorDao
import com.miso.vinilo.data.database.entities.CollectorEntity
import com.miso.vinilo.data.dto.CollectorDto
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CollectorRepositoryTest {

    @Test
    fun `forceRefresh fetches from network and stores in dao on success`() = runTest {
        // Arrange
        val adapter = mockk<NetworkServiceAdapterCollectors>()
        val dao = mockk<CollectorDao>(relaxUnitFun = true)
        val repository = CollectorRepository(adapter, dao)

        val dtoList = listOf(CollectorDto(1, "Manolo Bellon", "3502457896", "manollo@caracol.com.co"))
        coEvery { adapter.getCollectors() } returns NetworkResult.Success(dtoList)
        val entitySlot = slot<List<CollectorEntity>>()

        // Act
        repository.forceRefresh()

        // Assert
        coVerify(ordering = Ordering.SEQUENCE) {
            dao.deleteAll()
            dao.insertAll(capture(entitySlot))
        }
        assertEquals(1, entitySlot.captured.size)
        assertEquals(dtoList.first().id, entitySlot.captured.first().id)
        assertEquals(dtoList.first().name, entitySlot.captured.first().name)
    }

    @Test
    fun `forceRefresh does not touch dao on network error`() = runTest {
        // Arrange
        val adapter = mockk<NetworkServiceAdapterCollectors>()
        val dao = mockk<CollectorDao>(relaxUnitFun = true)
        val repository = CollectorRepository(adapter, dao)

        coEvery { adapter.getCollectors() } returns NetworkResult.Error("Network failure")

        // Act
        repository.forceRefresh()

        // Assert
        coVerify(exactly = 0) { dao.deleteAll() }
        coVerify(exactly = 0) { dao.insertAll(any()) }
    }

    @Test
    fun `refreshIfNeeded fetches when cache is old`() = runTest {
        // Arrange
        val adapter = mockk<NetworkServiceAdapterCollectors>()
        val dao = mockk<CollectorDao>(relaxUnitFun = true)
        val repository = CollectorRepository(adapter, dao)
        val thirtyMinutes = 1800000L

        coEvery { dao.getLastUpdateTime() } returns System.currentTimeMillis() - (thirtyMinutes + 1)
        coEvery { adapter.getCollectors() } returns NetworkResult.Success(emptyList())

        // Act
        repository.refreshIfNeeded()

        // Assert
        coVerify { adapter.getCollectors() }
    }

    @Test
    fun `refreshIfNeeded does not fetch when cache is fresh`() = runTest {
        // Arrange
        val adapter = mockk<NetworkServiceAdapterCollectors>()
        val dao = mockk<CollectorDao>(relaxUnitFun = true)
        val repository = CollectorRepository(adapter, dao)

        coEvery { dao.getLastUpdateTime() } returns System.currentTimeMillis()

        // Act
        repository.refreshIfNeeded()

        // Assert
        coVerify(exactly = 0) { adapter.getCollectors() }
    }

    @Test
    fun `refreshIfNeeded fetches when cache is null`() = runTest {
        // Arrange
        val adapter = mockk<NetworkServiceAdapterCollectors>()
        val dao = mockk<CollectorDao>(relaxUnitFun = true)
        val repository = CollectorRepository(adapter, dao)

        coEvery { dao.getLastUpdateTime() } returns null
        coEvery { adapter.getCollectors() } returns NetworkResult.Success(emptyList())

        // Act
        repository.refreshIfNeeded()

        // Assert
        coVerify { adapter.getCollectors() }
    }
}