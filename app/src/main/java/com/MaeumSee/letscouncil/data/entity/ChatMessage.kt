package com.min.maeumsee.data.entity

import java.io.Serializable

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable