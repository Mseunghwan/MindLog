package com.example.letscouncil

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.letscouncil.databinding.ActivityMainBinding
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상태바 설정
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = getColor(android.R.color.transparent)

        // 버튼 효과 설정
        setupClickEffects()

        // AI 메뉴 버튼 클릭 이벤트
        binding.aiMenuButton.setOnClickListener {
            val intent = Intent(this, AIMenuActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupClickEffects() {
        val cards = listOf(
            binding.btnWrite,   // XML ID: btn_write
            binding.btnCounsel, // XML ID: btn_counsel
            binding.btnDiary
        )

        cards.forEach { card ->
            // 카드 터치 효과
            card.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .start()
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()

                        if (event.action == MotionEvent.ACTION_UP) {
                            handleCardClick(card)
                        }
                    }
                }
                true
            }
        }
    }

    private fun handleCardClick(card: MaterialCardView) {
        when (card.id) {
            R.id.btn_write -> startActivity(Intent(this, WriteActivity::class.java))
            R.id.btn_counsel -> startActivity(Intent(this, CounselActivity::class.java))
            R.id.btn_diary -> startActivity(Intent(this, DataActivity::class.java))
        }
    }
}
