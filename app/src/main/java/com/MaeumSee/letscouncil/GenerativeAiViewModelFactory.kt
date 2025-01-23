// GenerativeAiViewModelFactory.kt
package com.min.maeumsee

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.MaeumSee.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.min.maeumsee.feature.chat.ChatViewModel

class GenerativeAiViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    // APIKEY는 local.properties로 관리하여 유출을 방지합니다
    private val apiKey = BuildConfig.GEMINI_API_KEY

    override fun <T : ViewModel> create(viewModelClass: Class<T>): T {
        val config = generationConfig {
            temperature = 0.7f
            maxOutputTokens = 500
            topP = 0.9f
        }

        return with(viewModelClass) {
            when {
                isAssignableFrom(ChatViewModel::class.java) -> {
                    val generativeModel = GenerativeModel(
                        modelName = "gemini-1.5-flash-latest",
                        apiKey = apiKey,
                        generationConfig = config
                    )
                    ChatViewModel(generativeModel, application)
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${viewModelClass.name}")
            }
        } as T
    }
}