package com.google.ai.sample.feature.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.ai.client.generativeai.GenerativeModel

class ChatViewModel(private val generativeModel: GenerativeModel) : ViewModel() {
    private val _chatMessages = MutableLiveData<List<ChatMessage>>(emptyList())
    val chatMessages: LiveData<List<ChatMessage>> get() = _chatMessages

    fun sendMessage(userMessage: String) {
        // 사용자 메시지 추가
        val newMessages = _chatMessages.value.orEmpty() + ChatMessage(userMessage, isUser = true)
        _chatMessages.postValue(newMessages)

        // Gemini API 호출로 AI 응답 생성
        val aiResponse = generativeModel.generate(userMessage)
        val updatedMessages = _chatMessages.value.orEmpty() + ChatMessage(aiResponse, isUser = false)
        _chatMessages.postValue(updatedMessages)
    }
}
