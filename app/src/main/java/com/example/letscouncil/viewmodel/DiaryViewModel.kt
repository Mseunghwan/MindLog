package com.example.letscouncil.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.letscouncil.data.database.DiaryDatabase
import com.example.letscouncil.data.entity.DiaryEntry
import kotlinx.coroutines.launch

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    // 데이터베이스 초기화
    private val database = DiaryDatabase.getDatabase(application)

    // DAO 초기화
    private val diaryDao = database.diaryDao()

    /**
     * 새로운 일기를 저장
     * @param content 일기의 내용
     * @param date 저장할 날짜 (기본값은 현재 시간)
     */
    fun saveDiary(content: String, date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            try {
                val entry = DiaryEntry(
                    date = date,
                    content = content
                )
                diaryDao.insert(entry)
                Log.d("DiaryViewModel", "Diary saved: $entry")
            } catch (e: Exception) {
                Log.e("DiaryViewModel", "Error saving diary: ${e.message}", e)
            }
        }
    }

    /**
     * 저장된 모든 일기 항목을 가져옴
     * @return LiveData 형식으로 일기 데이터를 반환
     */
    fun getAllEntries(): LiveData<List<DiaryEntry>> {
        return diaryDao.getAllEntries().asLiveData()
    }

    /**
     * 특정 날짜의 일기를 가져옴
     * @param date 조회할 날짜 (타임스탬프)
     * @return LiveData 형태로 반환 (nullable)
     */
    fun getEntryByDate(date: Long): LiveData<DiaryEntry?> {
        return diaryDao.getEntryByDate(date).asLiveData()
    }

    /**
     * 특정 일기 항목 삭제
     * @param entry 삭제할 일기 항목
     */
    fun deleteDiary(entry: DiaryEntry) {
        viewModelScope.launch {
            try {
                diaryDao.delete(entry)
                Log.d("DiaryViewModel", "Diary deleted: $entry")
            } catch (e: Exception) {
                Log.e("DiaryViewModel", "Error deleting diary: ${e.message}", e)
            }
        }
    }

    /**
     * 일기 업데이트
     * @param entry 업데이트할 일기 항목
     */
    fun updateDiary(entry: DiaryEntry) {
        viewModelScope.launch {
            try {
                diaryDao.update(entry)
                Log.d("DiaryViewModel", "Diary updated: $entry")
            } catch (e: Exception) {
                Log.e("DiaryViewModel", "Error updating diary: ${e.message}", e)
            }
        }
    }
}
