package com.example.letscouncil

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letscouncil.adapter.DiaryAdapter
import com.example.letscouncil.data.database.DiaryDatabase
import com.example.letscouncil.data.entity.DiaryEntry
import com.example.letscouncil.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var diaryAdapter: DiaryAdapter
    private val diaryDatabase by lazy { DiaryDatabase.getDatabase(this) }

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

        // 다이어리 버튼 클릭 이벤트
        binding.btnDiary.setOnClickListener {
            startActivity(Intent(this, DataActivity::class.java))
        }

        // 예제 데이터 추가 (테스트용)
        // addSampleDiaryData()
    }

    private fun setupClickEffects() {
        val cards = listOf(
            binding.btnWrite,
            binding.btnCounsel,
            binding.btnDiary
        )

        cards.forEach { card ->
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

    private fun handleCardClick(card: View) {
        when (card.id) {
            R.id.btn_write -> startActivity(Intent(this, WriteActivity::class.java))
            R.id.btn_counsel -> startActivity(Intent(this, CounselActivity::class.java))
            R.id.btn_diary -> startActivity(Intent(this, DataActivity::class.java))
        }
    }

    private fun addSampleDiaryData() {
        lifecycleScope.launch {
            val sampleData = listOf(
                DiaryEntry(content = "첫 번째 일기 내용", mood = "행복"),
                DiaryEntry(content = "두 번째 일기 내용", mood = "슬픔"),
                DiaryEntry(content = "세 번째 일기 내용", mood = "중립")
            )
            sampleData.forEach { diaryDatabase.diaryDao().insert(it) }
        }
    }
}
