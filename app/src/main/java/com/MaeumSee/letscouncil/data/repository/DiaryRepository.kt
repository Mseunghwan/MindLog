package com.min.mindlog.data.repository

import androidx.lifecycle.LiveData
import com.min.mindlog.data.dao.DiaryDao
import com.min.mindlog.data.entity.AnalysisResult
import com.min.mindlog.data.entity.DiaryEntry
import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val diaryDao: DiaryDao) {
    val allEntries: Flow<List<DiaryEntry>> = diaryDao.getAllEntries()

    fun getRecentEntries(limit: Int): LiveData<List<DiaryEntry>> {
        return diaryDao.getRecentEntries(limit)
    }

    suspend fun insert(entry: DiaryEntry) {
        diaryDao.insert(entry)
    }

    fun getLatestAnalysis() = diaryDao.getLatestAnalysis()

    fun getLatestDiaryDate() = diaryDao.getLatestDiaryDate()

    suspend fun saveAnalysis(analysis: AnalysisResult) {
        diaryDao.insertAnalysis(analysis)
    }

}