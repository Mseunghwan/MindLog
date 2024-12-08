package com.example.letscouncil.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.letscouncil.data.dao.DiaryDao
import com.example.letscouncil.data.entity.DiaryEntry

@Database(entities = [DiaryEntry::class], version = 1, exportSchema = false)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao

    companion object {
        @Volatile
        private var INSTANCE: DiaryDatabase? = null

        fun getDatabase(context: Context): DiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    "diary_database"
                ).fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
