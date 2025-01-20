package com.example.letscouncil
import android.os.Bundle
import android.transition.TransitionInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letscouncil.databinding.ActiivityAiBinding
import com.example.letscouncil.feature.chat.ChatAdapter
import com.example.letscouncil.feature.chat.ChatViewModel
import com.example.letscouncil.GenerativeViewModelFactory
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class AIMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActiivityAiBinding
    private val chatViewModel: ChatViewModel by viewModels { GenerativeViewModelFactory }
    private val chatAdapter = ChatAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        // 액티비티 전환 애니메이션 설정
        window.exitTransition = TransitionInflater.from(this)
            .inflateTransition(R.transition.activity_fade)

        super.onCreate(savedInstanceState)
        binding = ActiivityAiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupChatRecyclerView()
        setupListeners()
        observeViewModel()
        setupObservers()
        setupQuickResponses()
        setupMoodButton()
    }

    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupChatRecyclerView() {
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AIMenuActivity)
            adapter = chatAdapter
        }
    }

    private fun setupListeners() {
        binding.sendButton.setOnClickListener {
            val userMessage = binding.chatInput.text.toString()
            if (userMessage.isNotBlank()) {
                lifecycleScope.launch {
                    chatViewModel.sendMessage(userMessage)
                }
                binding.chatInput.text.clear()
            }
        }
    }

    private fun observeViewModel() {
        chatViewModel.chatMessages.observe(this) { messages ->
            chatAdapter.submitList(messages)
            binding.chatRecyclerView.scrollToPosition(messages.size - 1)
        }
    }

    private fun setupObservers() {
        // 채팅 메시지 관찰
        chatViewModel.chatMessages.observe(this) { messages ->
            chatAdapter.submitList(messages)
            binding.chatRecyclerView.scrollToPosition(messages.size - 1)
        }

        // 감정 상태 관찰
        chatViewModel.currentMood.observe(this) { mood ->
            binding.moodChip.text = "기분: ${mood.emoji}"
            binding.emotionButton.setImageResource(
                when (mood) {
                    ChatViewModel.Mood.VERY_HAPPY -> R.drawable.ic_mood
                    ChatViewModel.Mood.SAD -> R.drawable.ic_mood
                    else -> R.drawable.ic_mood
                }
            )
        }

        // 대화 진행도 관찰
        chatViewModel.conversationProgress.observe(this) { progress ->
            binding.moodProgress.progress = progress
            when (progress) {
                in 0..30 -> "대화를 시작해볼까요?"
                in 31..60 -> "대화가 잘 진행되고 있어요!"
                in 61..90 -> "정말 좋은 대화네요 ✨"
                else -> "오늘도 좋은 대화 였어요 💫"
            }.also { binding.progressText.text = it }
        }
    }

    private fun setupQuickResponses() {
        chatViewModel.quickResponses.forEach { response ->
            val chip = createQuickResponseChip(response)
            binding.quickResponseGroup.addView(chip)
        }
    }

    private fun createQuickResponseChip(response: ChatViewModel.QuickResponse): Chip {
        return Chip(this).apply {
            text = "${response.text} ${response.emoji}"
            isCheckable = false
            setOnClickListener {
                lifecycleScope.launch {
                    chatViewModel.sendQuickResponse(response)
                }
            }
        }
    }

    private fun setupMoodButton() {
        binding.emotionButton.setOnClickListener {
            showMoodSelectionDialog()
        }
    }

    private fun showMoodSelectionDialog() {
        val moods = ChatViewModel.Mood.values()
        val items = moods.map { "${it.emoji} ${it.description}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("지금 기분이 어떠신가요?")
            .setItems(items) { _, which ->
                lifecycleScope.launch {
                    chatViewModel.sendMessage("지금 제 기분은 ${moods[which].description}이에요 ${moods[which].emoji}")
                }
            }
            .show()
    }
}