package com.google.ai.sample.feature.chat

data class ChatMessage(
    val content: String,
    val isUser: Boolean // true면 사용자 메시지, false면 AI 응답
)
