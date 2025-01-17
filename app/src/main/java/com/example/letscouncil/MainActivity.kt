package com.example.letscouncil

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.letscouncil.data.UserPreferences
import com.example.letscouncil.data.database.DiaryDatabase
import com.example.letscouncil.data.entity.DiaryEntry
import com.example.letscouncil.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val diaryDatabase by lazy { DiaryDatabase.getDatabase(this) }
    private val levelTitles = listOf(
        "새싹 일기장",         // 1
        "초보 기록가",         // 2
        "글씨 연습생",         // 3
        "일기 초심자",         // 4
        "꾸준한 필사자",       // 5
        "하루의 설계자",       // 6
        "소소한 기록가",       // 7
        "성실한 이야기꾼",     // 8
        "일기 마스터 초보",    // 9
        "꾸준함의 씨앗",       // 10
        "책임감의 모범생",     // 11
        "작은 성공의 설계자",  // 12
        "성실함의 챔피언",     // 13
        "기록의 탐험가",       // 14
        "일기 마스터",         // 15
        "꾸준함의 달인",       // 16
        "하루의 조각가",       // 17
        "성실함의 거장",       // 18
        "인생 기록가",         // 19
        "꾸준함의 전설"        // 20
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userPreferences = UserPreferences(this)

        // 사용자 설정 여부 확인
        val user = userPreferences.getUser()
        if (user == null) {
            // 사용자 정보가 없으면 UserSetupActivity로 이동
            startActivity(Intent(this, UserSetupActivity::class.java))
            finish()
            return
        }
        binding.welcomeText.text = "${user.name}님! 좋은 하루에요."

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

        // 유저레벨 프로그레스바 설정
        val status = user.score / 100
        binding.progressText.text = "Lv. ${(status+1).toString()} \n${levelTitles[status].toString()}"
        binding.levelProgressBar.progress=(user.score%100)


        // 예제 데이터 추가 (테스트용)
        // addSampleDiaryData()
    }

    override fun onResume() {
        super.onResume()

        val userPreferences = UserPreferences(this)
        val user = userPreferences.getUser()

        if (user != null) {
            val status = user.score / 100
            binding.progressText.text = "Lv. ${(status+1).toString()} \n${levelTitles[status].toString()}"
            binding.levelProgressBar.progress = (user.score % 100)
        } else {
            // 만약 유저 정보가 없을 경우
            binding.welcomeText.text = "유저 정보를 불러오지 못했습니다."
        }
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
