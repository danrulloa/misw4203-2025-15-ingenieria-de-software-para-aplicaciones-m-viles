package com.miso.vinilo.data.repository

import androidx.paging.PagingSource
import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.adapter.NetworkServiceAdapterMusicians
import com.miso.vinilo.data.database.dao.MusicianDao
import com.miso.vinilo.data.database.entities.MusicianEntity
import com.miso.vinilo.data.dto.AlbumDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MusicianRepositoryImplTest {

    @Test
    fun `getPagedMusicians returns flow from dao`() = runTest {
        val adapter = mockk<NetworkServiceAdapterMusicians>(relaxed = true)
        val dao = mockk<MusicianDao>()
        val pagingSource = mockk<PagingSource<Int, MusicianEntity>>()
        every { dao.getPagedMusicians() } returns pagingSource

        val repo = MusicianRepository(adapter, dao)
        val flow = repo.getPagedMusicians()

        // Ensure flow object is created
        assertNotNull(flow)
    }

    @Test
    fun `forceRefresh fetches from network and stores in dao`() = runTest {
        val adapter = mockk<NetworkServiceAdapterMusicians>()
        val dao = mockk<MusicianDao>(relaxed = true)
        val musicians = listOf(
            MusicianDto(1, "Adele", "", "Singer", "1988-05-05T00:00:00.000Z")
        )
        coEvery { adapter.getMusicians() } returns NetworkResult.Success(musicians)
        coEvery { dao.deleteAll() } returns Unit
        coEvery { dao.insertAll(any()) } returns Unit

        val repo = MusicianRepository(adapter, dao)
        repo.forceRefresh()

        coVerify { dao.deleteAll() }
        coVerify { dao.insertAll(any()) }
    }

    @Test
    fun `refreshIfNeeded does not refresh if data is fresh`() = runTest {
        val adapter = mockk<NetworkServiceAdapterMusicians>(relaxed = true)
        val dao = mockk<MusicianDao>(relaxed = true)
        val recentTimestamp = System.currentTimeMillis() - 60000L // 1 minute ago
        coEvery { dao.getLastUpdateTime() } returns recentTimestamp

        val repo = MusicianRepository(adapter, dao)
        repo.refreshIfNeeded()

        // Should not call network since data is fresh (< 30 min)
        coVerify(exactly = 0) { adapter.getMusicians() }
    }

    @Test
    fun `refreshIfNeeded refreshes if data is stale`() = runTest {
        val adapter = mockk<NetworkServiceAdapterMusicians>()
        val dao = mockk<MusicianDao>(relaxed = true)
        val staleTimestamp = System.currentTimeMillis() - 3600000L // 1 hour ago
        coEvery { dao.getLastUpdateTime() } returns staleTimestamp
        coEvery { adapter.getMusicians() } returns NetworkResult.Success(emptyList())
        coEvery { dao.deleteAll() } returns Unit
        coEvery { dao.insertAll(any()) } returns Unit

        val repo = MusicianRepository(adapter, dao)
        repo.refreshIfNeeded()

        // Should call network since data is stale (> 30 min)
        coVerify { adapter.getMusicians() }
        coVerify { dao.deleteAll() }
    }

    @Test
    fun `refreshIfNeeded refreshes if no data exists`() = runTest {
        val adapter = mockk<NetworkServiceAdapterMusicians>()
        val dao = mockk<MusicianDao>(relaxed = true)
        coEvery { dao.getLastUpdateTime() } returns null // No data
        coEvery { adapter.getMusicians() } returns NetworkResult.Success(emptyList())
        coEvery { dao.deleteAll() } returns Unit
        coEvery { dao.insertAll(any()) } returns Unit

        val repo = MusicianRepository(adapter, dao)
        repo.refreshIfNeeded()

        // Should call network since no data exists
        coVerify { adapter.getMusicians() }
    }

    @Test
    fun `getMusician delegates call to serviceAdapter and returns NetworkResult`() = runTest {
        val adapter = mockk<NetworkServiceAdapterMusicians>()
        val dao = mockk<MusicianDao>(relaxed = true)

        val dto = MusicianDto(
            id = 100L,
            name = "Rubén Blades Bellido de Luna",
            image = "url",
            description = "desc",
            birthDate = "1948-07-16T00:00:00.000Z",
            albums = emptyList(),
        )

        coEvery { adapter.getMusician(100L) } returns NetworkResult.Success(dto)

        val repository = MusicianRepository(adapter, dao)
        val result = repository.getMusician(100L)

        coVerify { adapter.getMusician(100L) }
        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals("Rubén Blades Bellido de Luna", data.name)
    }

    @Test
    fun `addAlbumToMusician returns Success when adapter succeeds`() = runTest {
        val adapter = mockk<NetworkServiceAdapterMusicians>()
        val dao = mockk<MusicianDao>(relaxed = true)

        val albumDto = AlbumDto(
            id = 102,
            name = "A Night at the Opera",
            cover = "url",
            releaseDate = "1975-11-21T00:00:00.000Z",
            description = "desc",
            genre = "Rock",
            recordLabel = "EMI",
            tracks = emptyList(),
            performers = emptyList(),
            comments = emptyList()
        )

        coEvery { adapter.addAlbumToMusician(100L, 102L) } returns NetworkResult.Success(albumDto)

        val repo = MusicianRepository(adapter, dao)
        val result = repo.addAlbumToMusician(100L, 102L)

        coVerify { adapter.addAlbumToMusician(100L, 102L) }
        assertTrue(result is NetworkResult.Success)
        assertEquals(albumDto, (result as NetworkResult.Success).data)
    }

    @Test
    fun `addAlbumToMusician returns Error when adapter fails`() = runTest {
        val adapter = mockk<NetworkServiceAdapterMusicians>()
        val dao = mockk<MusicianDao>(relaxed = true)

        coEvery { adapter.addAlbumToMusician(100L, 999L) } returns NetworkResult.Error(
            message = "Error al agregar álbum al artista",
            throwable = RuntimeException("Network down")
        )

        val repo = MusicianRepository(adapter, dao)
        val result = repo.addAlbumToMusician(100L, 999L)

        assertTrue(result is NetworkResult.Error)
        assertEquals(
            "Error al agregar álbum al artista",
            (result as NetworkResult.Error).message
        )
        assertNotNull(result.throwable)
    }



}
