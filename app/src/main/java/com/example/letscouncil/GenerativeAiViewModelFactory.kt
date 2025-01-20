// GenerativeAiViewModelFactory.kt
package com.example.letscouncil

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.example.letscouncil.feature.chat.ChatViewModel

class GenerativeAiViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    private val apiKey = "AIzaSyB3t-QgoNfPSzmcIw1I7B7SJ2rbjneinq0"

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