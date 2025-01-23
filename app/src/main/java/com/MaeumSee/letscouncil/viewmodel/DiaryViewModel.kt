package com.min.maeumsee.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.min.maeumsee.data.dao.DiaryDao
import com.min.maeumsee.data.database.DiaryDatabase
import com.min.maeumsee.data.entity.DiaryEntry
import com.min.maeumsee.data.entity.AnalysisResult
import com.min.maeumsee.data.repository.DiaryRepository
import com.min.maeumsee.feature.chat.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.Date

class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DiaryRepository
    private val _allEntries: LiveData<List<DiaryEntry>>
    private val diaryDao: DiaryDao

    init {
        val database = DiaryDatabase.getDatabase(application)
        diaryDao = database.diaryDao()
        repository = DiaryRepository(diaryDao)
        _allEntries = repository.allEntries.asLiveData()
    }

    // 이번 달의 일기 개수를 LiveData로 변환
    fun getCurrentMonthEntriesCount(): LiveData<Int> {
        return diaryDao.getCurrentMonthEntriesCount().asLiveData()
    }

    fun getAllEntries(): LiveData<List<DiaryEntry>> = _allEntries

    fun insert(entry: DiaryEntry) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(entry)
    }

    fun getRecentEntries(limit: Int): LiveData<List<DiaryEntry>> {
        return repository.getRecentEntries(limit)
    }

    fun saveDiary(content: String) = viewModelScope.launch(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        val entry = DiaryEntry(
            date = currentTime,
            content = content
        )
        repository.insert(entry)
    }

    fun getLatestAnalysis(): LiveData<AnalysisResult?> =
        repository.getLatestAnalysis().asLiveData()

    fun shouldReanalyze(): Flow<Boolean> = flow {
        combine(
            repository.getLatestAnalysis(),
            repository.getLatestDiaryDate()
        ) { analysis, latestDiaryDate ->
            if (analysis == null) true
            else latestDiaryDate != null && latestDiaryDate > analysis.lastAnalyzedDiaryDate
        }.collect { emit(it) }
    }

    fun saveAnalysisResult(analysis: AnalysisResult) = viewModelScope.launch {
        repository.saveAnalysis(analysis)
    }

    fun getMoodForDate(date: Date): ChatViewModel.Mood? {
        // Room DB에서 해당 날짜의 일기 데이터를 조회하여 감정 반환
        viewModelScope.launch {
            val diary = diaryDao.getEntryByDate(date.time).firstOrNull()
            // 일기의 감정 분석 결과를 Mood로 변환하여 반환
            diary?.let {
                // 감정 분석 결과를 기반으로 적절한 Mood 반환
                // 이 부분은 감정 분석 결과 저장 방식에 따라 구현
            }
        }
        return null  // 임시 반환값
    }

    fun getEntryByDate(date: Long): LiveData<DiaryEntry?> {
        return diaryDao.getEntryByDate(date).asLiveData()
    }



}