package com.example.letscouncil.feature.chat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.ai.client.generativeai.GenerativeModel

class ChatViewModel(private val generativeModel: GenerativeModel) : ViewModel() {
    private val _chatMessages = MutableLiveData<List<ChatMessage>>(listOf(
        ChatMessage(content = "오늘 하루 어땠어?", isUser = false) // 초기 AI 메시지
    ))

    private val _chatHistory = MutableLiveData<List<ChatMessage>>(emptyList()) // 맥락 관련 설정 추가
    val chatHistory: LiveData<List<ChatMessage>> get() = _chatHistory // 맥락 관련 설정 추가


    val chatMessages: LiveData<List<ChatMessage>> get() = _chatMessages

    private fun updateChatHistory(newMessage: ChatMessage) {
        val currentHistory = _chatHistory.value.orEmpty()
        _chatHistory.postValue(currentHistory + newMessage)
    } // 맥락 관련 설정 추가

    suspend fun sendMessage(userMessage: String) {
        try {
            // 사용자 메시지 추가
            val userMessageObject = ChatMessage(userMessage, isUser = true)
            Log.d("UserMessage", "Adding user message: $userMessageObject") // 사용자 메시지 디버깅
            val currentMessages = _chatMessages.value.orEmpty()
            _chatMessages.postValue(currentMessages + userMessageObject)

            // 메시지 리스트 디버깅
            Log.d("ChatMessages", "Updated chat messages: ${_chatMessages.value}")

            // 대화 기록 업데이트
            updateChatHistory(userMessageObject)

            // 프롬프트 생성 및 AI 호출
            val engineeredPrompt = preparePrompt(userMessage)
            val chat = generativeModel.startChat()

            // AI 응답 생성
            val response = chat.sendMessage(engineeredPrompt)
            val responseText = response.text?.toString() ?: "응답을 생성할 수 없습니다"
            val responseMessage = ChatMessage(responseText, isUser = false)

            // AI 응답 추가 및 디버깅
            _chatMessages.postValue(_chatMessages.value.orEmpty() + responseMessage)
            Log.d("ChatMessages", "AI response added: ${_chatMessages.value}")
            updateChatHistory(responseMessage)
        } catch (e: Exception) {
            val errorMessage = ChatMessage("문제가 발생했어요. 다시 시도해 주세요!", isUser = false)
            _chatMessages.postValue(_chatMessages.value.orEmpty() + errorMessage)
            Log.e("ChatMessages", "Error occurred: $e")
            updateChatHistory(errorMessage)
        }
    }



    private fun preparePrompt(userMessage: String): String {
        // 최근 대화 기록 가져오기
        val recentHistory = _chatHistory.value.orEmpty().takeLast(5).joinToString("\n") { message ->
            if (message.isUser) "사용자: ${message.content}" else "친구: ${message.content}"
        }

        val truncatedMessage = if (userMessage.length > 300) {
            userMessage.take(300) + "..."
        } else {
            userMessage
        }

        // 사용자 메시지 길이에 따라 다른 지침 제공
        val responseStyle = if (truncatedMessage.length > 100) {
           "조금 더 구체적이고 진심 어린 공감과 격려를 포함해주세요."
        } else {
            "간결하고 따뜻하게 공감의 말을 전해주세요."

        }

        // 최종 프롬프트 생성
        return """
        대화 요약:
        최근 사용자는 이런 대화를 했습니다:
        $recentHistory

        현재 사용자의 메시지:
        사용자: $truncatedMessage

        이 맥락을 반영하여 자연스럽게 친구처럼 대화하듯이 응답해주세요. 
        사용자의 감정을 이해하고, 필요하다면 간단히 질문을 추가하거나 
        대화를 이어갈 수 있는 자연스러운 답변을 작성하세요. 
        $responseStyle
        """.trimIndent()
    }
}
