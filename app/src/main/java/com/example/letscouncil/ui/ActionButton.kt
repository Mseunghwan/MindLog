package com.example.letscouncil.ui

object ActionButtonId {
    const val WRITE = 1001
    const val ANALYSIS = 1002
    const val JOURNAL = 1003
    const val CHAT = 1004
}

data class ActionButton(
    val id: Int,
    val title: String,
    val description: String,
    val iconResId: Int
)