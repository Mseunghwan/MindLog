package com.example.letscouncil.data.model

data class AggregatedAnalysis(
    val positivePercent: Float,
    val neutralPercent: Float,
    val negativePercent: Float,
    val emotionKeywords: List<String>,
    val recommendations: List<String>
)