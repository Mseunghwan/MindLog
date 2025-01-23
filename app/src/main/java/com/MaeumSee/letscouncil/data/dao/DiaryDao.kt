package com.min.mindlog.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.min.mindlog.data.entity.AnalysisResult
import com.min.mindlog.data.entity.DiaryEntry
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


    @Query("SELECT * FROM diary_entries ORDER BY date DESC LIMIT :limit")
    fun getRecentEntries(limit: Int): LiveData<List<DiaryEntry>>  // Flow를 LiveData로 변경

    @Query("SELECT * FROM analysis_results ORDER BY timestamp DESC LIMIT 1")
    fun getLatestAnalysis(): Flow<AnalysisResult?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AnalysisResult)

    @Query("SELECT MAX(date) FROM diary_entries")
    fun getLatestDiaryDate(): Flow<Long?>

    @Query("""
        SELECT COUNT(*) FROM diary_entries 
        WHERE strftime('%Y-%m', datetime(date/1000, 'unixepoch')) = 
        strftime('%Y-%m', 'now')
    """)
    fun getCurrentMonthEntriesCount(): Flow<Int>

}
