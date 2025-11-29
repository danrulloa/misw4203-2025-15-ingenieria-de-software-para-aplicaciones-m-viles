package com.miso.vinilo.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.miso.vinilo.data.database.entities.CollectorEntity

@Dao
interface CollectorDao {

    @Query("SELECT * FROM collectors ORDER BY id")
    fun getPagedCollectors(): PagingSource<Int, CollectorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(collectors: List<CollectorEntity>)

    @Query("DELETE FROM collectors")
    suspend fun deleteAll()

    @Query("SELECT MAX(lastUpdated) FROM collectors")
    suspend fun getLastUpdateTime(): Long?
}
