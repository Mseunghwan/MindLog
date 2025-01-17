package com.example.letscouncil
import android.os.Bundle
import android.transition.TransitionInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letscouncil.databinding.ActiivityAiBinding
import com.example.letscouncil.feature.chat.ChatAdapter
import com.example.letscouncil.feature.chat.ChatViewModel
import com.example.letscouncil.GenerativeViewModelFactory
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
    }


    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar) // XML에 정의된 Toolbar를 앱바로 설정
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
            setDisplayShowHomeEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
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
}