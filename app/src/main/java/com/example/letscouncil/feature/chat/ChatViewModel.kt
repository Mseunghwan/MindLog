package com.example.letscouncil.feature.chat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.ai.client.generativeai.GenerativeModel

class ChatViewModel(private val generativeModel: GenerativeModel) : ViewModel() {
    private val _chatMessages = MutableLiveData<List<ChatMessage>>(emptyList())
    val chatMessages: LiveData<List<ChatMessage>> get() = _chatMessages

    suspend fun sendMessage(userMessage: String) {
        val newMessages = _chatMessages.value.orEmpty() + ChatMessage(userMessage, isUser = true)
        _chatMessages.postValue(newMessages)

        val chat = generativeModel.startChat()
        val response = chat.sendMessage(userMessage)
        val updatedMessages = _chatMessages.value.orEmpty() + ChatMessage(response.text?.toString() ?: "응답을 생성할 수 없습니다", isUser = false)
        _chatMessages.postValue(updatedMessages)
    }
}
