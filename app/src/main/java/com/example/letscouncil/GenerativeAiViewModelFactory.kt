package com.example.letscouncil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.example.letscouncil.feature.chat.ChatViewModel

val GenerativeViewModelFactory = object : ViewModelProvider.Factory {
    private val apiKey = "APIKey"  // 실제 API 키로 교체하세요

    override fun <T : ViewModel> create(
        viewModelClass: Class<T>,
        extras: CreationExtras
    ): T {
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
                    ChatViewModel(generativeModel)
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${viewModelClass.name}")
            }
        } as T
    }
}