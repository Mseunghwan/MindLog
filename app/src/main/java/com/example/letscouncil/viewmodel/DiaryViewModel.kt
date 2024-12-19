package com.example.letscouncil.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.letscouncil.data.database.DiaryDatabase
import com.example.letscouncil.data.entity.DiaryEntry
import com.example.letscouncil.data.entity.AnalysisResult
import com.example.letscouncil.data.repository.DiaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DiaryRepository
    private val _allEntries: LiveData<List<DiaryEntry>>

    init {
        val diaryDao = DiaryDatabase.getDatabase(application).diaryDao()
        repository = DiaryRepository(diaryDao)
        _allEntries = repository.allEntries.asLiveData()
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
}