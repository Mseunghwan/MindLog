package com.example.letscouncil.feature.chat
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class ChatViewModel(
    private val generativeModel: GenerativeModel,
    application: Application
) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences(
        "chat_preferences", Context.MODE_PRIVATE
    )

    private val _chatMessages = MutableLiveData<List<ChatMessage>>(listOf(
        ChatMessage(content = "오늘 하루 어땠어?", isUser = false)
    ))
    val chatMessages: LiveData<List<ChatMessage>> get() = _chatMessages

    private val _chatHistory = MutableLiveData<List<ChatMessage>>(emptyList())
    val chatHistory: LiveData<List<ChatMessage>> get() = _chatHistory

    init {
        loadChatHistory()
    }

    private fun loadChatHistory() {
        val lastSavedDate = sharedPreferences.getLong("last_saved_date", 0)
        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // 날짜가 변경되었는지 확인
        if (lastSavedDate < currentDate) {
            // 하루가 지났으면 초기화
            _chatMessages.value = listOf(ChatMessage(content = "오늘 하루 어땠어?", isUser = false))
            _chatHistory.value = emptyList()
            saveChatHistory(emptyList())
            sharedPreferences.edit().putLong("last_saved_date", currentDate).apply()
        } else {
            // 같은 날이면 저장된 대화 불러오기
            val savedMessages = getSavedMessages()
            if (savedMessages.isNotEmpty()) {
                _chatMessages.value = savedMessages
                _chatHistory.value = savedMessages
            }
        }
    }

    private fun saveChatHistory(messages: List<ChatMessage>) {
        val gson = Gson()
        val jsonMessages = gson.toJson(messages)
        sharedPreferences.edit()
            .putString("chat_messages", jsonMessages)
            .putLong("last_saved_date", System.currentTimeMillis())
            .apply()
    }

    private fun getSavedMessages(): List<ChatMessage> {
        val gson = Gson()
        val jsonMessages = sharedPreferences.getString("chat_messages", null)
        return if (jsonMessages != null) {
            try {
                gson.fromJson(jsonMessages, object : TypeToken<List<ChatMessage>>() {}.type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun updateChatHistory(newMessage: ChatMessage) {
        val currentMessages = _chatMessages.value.orEmpty()
        val updatedMessages = currentMessages + newMessage
        _chatMessages.postValue(updatedMessages)
        saveChatHistory(updatedMessages)
    }
    private val _currentMood = MutableLiveData<Mood>(Mood.NEUTRAL)
    val currentMood: LiveData<Mood> = _currentMood

    private val _conversationProgress = MutableLiveData<Int>(0)
    val conversationProgress: LiveData<Int> = _conversationProgress

    // 감정 상태 enum 클래스
    enum class Mood(val emoji: String, val description: String) {
        VERY_HAPPY("😊", "매우 좋음"),
        HAPPY("🙂", "좋음"),
        NEUTRAL("😐", "보통"),
        SAD("😢", "슬픔"),
        ANXIOUS("😰", "불안"),
        ANGRY("😠", "화남")
    }

    // 빠른 응답 데이터 클래스
    data class QuickResponse(
        val text: String,
        val emoji: String,
        val type: ResponseType
    )

    enum class ResponseType {
        EMPATHY, QUESTION, ENCOURAGEMENT, HAPPINESS
    }

    // 빠른 응답 목록
    val quickResponses = listOf(
        QuickResponse("그렇구나", "🤔", ResponseType.EMPATHY),
        QuickResponse("더 자세히 말해줘", "✨", ResponseType.QUESTION),
        QuickResponse("힘들었겠다", "😢", ResponseType.EMPATHY),
        QuickResponse("정말 기뻐", "🎉", ResponseType.HAPPINESS),
        QuickResponse("잘 했어!", "👍", ResponseType.ENCOURAGEMENT)
    )

    // 대화 진행도 업데이트
    private fun updateConversationProgress(message: String) {
        val currentProgress = _conversationProgress.value ?: 0
        val newProgress = when {
            message.length > 50 -> currentProgress + 15
            message.length > 20 -> currentProgress + 10
            else -> currentProgress + 5
        }
        _conversationProgress.value = minOf(newProgress, 100)
    }

    // 감정 분석 및 업데이트
    private fun updateMood(message: String) {
        // 여기에 감정 분석 로직 추가
        // 예시로 간단한 키워드 기반 분석
        val newMood = when {
            message.contains(Regex("행복|좋아|기쁘|즐거")) -> Mood.VERY_HAPPY
            message.contains(Regex("웃|재미|좋은|감사")) -> Mood.HAPPY
            message.contains(Regex("슬프|우울|힘들")) -> Mood.SAD
            message.contains(Regex("걱정|불안|두려")) -> Mood.ANXIOUS
            message.contains(Regex("화|짜증|싫")) -> Mood.ANGRY
            else -> Mood.NEUTRAL
        }
        _currentMood.value = newMood
    }

    // 메시지 전송 함수 수정
    suspend fun sendMessage(userMessage: String, isQuickResponse: Boolean = false) {
        try {
            val userMessageObject = ChatMessage(userMessage, isUser = true)
            _chatMessages.postValue(_chatMessages.value.orEmpty() + userMessageObject)
            updateChatHistory(userMessageObject)

            // 대화 진행도와 감정 상태 업데이트
            updateConversationProgress(userMessage)
            if (!isQuickResponse) {
                updateMood(userMessage)
            }

            // AI 응답 생성
            val engineeredPrompt = preparePrompt(userMessage)
            val chat = generativeModel.startChat()
            val response = chat.sendMessage(engineeredPrompt)
            val responseText = response.text?.toString() ?: "응답을 생성할 수 없습니다"
            val responseMessage = ChatMessage(responseText, isUser = false)

            _chatMessages.postValue(_chatMessages.value.orEmpty() + responseMessage)
            updateChatHistory(responseMessage)
        } catch (e: Exception) {
            val errorMessage = ChatMessage("죄송해요, 다시 한 번 말씀해 주시겠어요?", isUser = false)
            _chatMessages.postValue(_chatMessages.value.orEmpty() + errorMessage)
            Log.e("ChatViewModel", "Error: $e")
            updateChatHistory(errorMessage)
        }
    }

    // 빠른 응답 선택 처리
    suspend fun sendQuickResponse(response: QuickResponse) {
        sendMessage("${response.text} ${response.emoji}", isQuickResponse = true)
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
