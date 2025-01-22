// AnalysisResult.kt
package com.MaeumSee.letscouncil.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_results")
data class AnalysisResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val positivePercent: Float,
    val neutralPercent: Float,
    val negativePercent: Float,
    val emotionKeywords: String,  // JSON 문자열로 저장
    val recommendations: String,   // JSON 문자열로 저장
    val lastAnalyzedDiaryDate: Long  // 마지막으로 분석된 일기의 날짜
)