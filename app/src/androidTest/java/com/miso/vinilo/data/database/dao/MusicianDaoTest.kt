package com.miso.vinilo.data.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miso.vinilo.data.database.ViniloDatabase
import com.miso.vinilo.data.database.entities.MusicianEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MusicianDaoTest {

    private lateinit var database: ViniloDatabase
    private lateinit var dao: MusicianDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ViniloDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.musicianDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getPagingSourceReturnsNonNull() = runTest {
        val pagingSource = dao.getPagedMusicians()
        assertNotNull(pagingSource)
    }

    @Test
    fun getLastUpdateTimeReturnsMaxTimestamp() = runTest {
        val time1 = System.currentTimeMillis() - 1000
        val time2 = System.currentTimeMillis()

        val musicians = listOf(
            MusicianEntity(1, "Adele", null, "Singer", "1988-05-05T00:00:00.000Z", time1),
            MusicianEntity(2, "Metallica", null, "Band", "1981-10-28T00:00:00.000Z", time2)
        )

        dao.insertAll(musicians)

        val lastUpdate = dao.getLastUpdateTime()
        assertNotNull(lastUpdate)
        assertEquals(time2, lastUpdate)
    }

    @Test
    fun getLastUpdateTimeReturnsNullWhenEmpty() = runTest {
        val lastUpdate = dao.getLastUpdateTime()
        assertEquals(null, lastUpdate)
    }
}
