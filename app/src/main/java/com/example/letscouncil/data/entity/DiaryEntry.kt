package com.example.letscouncil.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val content: String,
    val mood: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
