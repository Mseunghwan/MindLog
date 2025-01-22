package com.MaeumSee.letscouncil.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.MaeumSee.letscouncil.data.dao.DiaryDao
import com.MaeumSee.letscouncil.data.entity.DiaryEntry
import com.MaeumSee.letscouncil.data.entity.AnalysisResult

@Database(
    entities = [DiaryEntry::class, AnalysisResult::class],  // AnalysisResult 엔티티 추가
    version = 3  // 버전 1에서 2로 증가 + 에서 3으로 증가
)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao

    companion object {
        @Volatile
        private var INSTANCE: DiaryDatabase? = null

        fun getDatabase(context: Context): DiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    "diary_database"
                )
                    .fallbackToDestructiveMigration()  // 데이터베이스 버전 변경 시 기존 데이터 삭제하고 새로 생성
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}