package com.miso.vinilo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.miso.vinilo.data.database.dao.AlbumDao
import com.miso.vinilo.data.database.dao.CollectorDao
import com.miso.vinilo.data.database.dao.MusicianDao
import com.miso.vinilo.data.database.entities.AlbumEntity
import com.miso.vinilo.data.database.entities.CollectorEntity
import com.miso.vinilo.data.database.entities.MusicianEntity

@Database(
    entities = [MusicianEntity::class, CollectorEntity::class, AlbumEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ViniloDatabase : RoomDatabase() {

    abstract fun musicianDao(): MusicianDao
    abstract fun collectorDao(): CollectorDao
    abstract fun albumDao(): AlbumDao

    companion object {
        @Volatile
        private var INSTANCE: ViniloDatabase? = null

        fun getDatabase(context: Context): ViniloDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ViniloDatabase::class.java,
                    "vinilo_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
