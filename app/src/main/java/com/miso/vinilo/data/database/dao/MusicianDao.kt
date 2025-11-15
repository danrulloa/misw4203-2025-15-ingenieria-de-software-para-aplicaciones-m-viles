package com.miso.vinilo.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.miso.vinilo.data.database.entities.MusicianEntity

@Dao
interface MusicianDao {

    @Query("SELECT * FROM musicians ORDER BY id")
    fun getPagedMusicians(): PagingSource<Int, MusicianEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(musicians: List<MusicianEntity>)

    @Query("DELETE FROM musicians")
    suspend fun deleteAll()

    @Query("SELECT MAX(lastUpdated) FROM musicians")
    suspend fun getLastUpdateTime(): Long?
}
