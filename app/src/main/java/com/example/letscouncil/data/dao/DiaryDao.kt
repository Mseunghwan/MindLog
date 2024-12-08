package com.example.letscouncil.data.dao

import androidx.room.*
import com.example.letscouncil.data.entity.DiaryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE date = :date LIMIT 1")
    fun getEntryByDate(date: Long): Flow<DiaryEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiaryEntry)

    @Update
    suspend fun update(entry: DiaryEntry)

    @Delete
    suspend fun delete(entry: DiaryEntry)
}
