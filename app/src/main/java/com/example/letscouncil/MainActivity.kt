package com.example.letscouncil

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.example.letscouncil.data.UserPreferences
import com.example.letscouncil.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
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

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        val userPreferences = UserPreferences(this)
        val user = userPreferences.getUser()
        // 사용자 설정 여부 확인
        if (user == null) {
            // 사용자 정보가 없으면 UserSetupActivity로 이동
            startActivity(Intent(this, UserSetupActivity::class.java))
            finish()
            return
        }
        // Welcome message based on time of day
        binding.welcomeText.text = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> user!!.name + getString(R.string.welcome_morning)
            in 12..16 -> user!!.name + getString(R.string.welcome_afternoon)
            else -> user!!.name + getString(R.string.welcome_evening)
        }

        // Date
        val dateFormat = SimpleDateFormat(getString(R.string.date_format), Locale.getDefault())
        binding.dateText.text = dateFormat.format(Date())

        // Level progress
        if (user != null) {
            val level = (user.score / 100) + 1
            binding.progressText.text = getString(R.string.level_format, level) +"      "+ levelTitles[level-1]
            binding.levelProgressBar.progress = user.score % 100
        }
    }
    override fun onResume() {
        super.onResume()

        val userPreferences = UserPreferences(this)
        val user = userPreferences.getUser()

        val level = (user!!.score / 100) + 1
        binding.progressText.text = getString(R.string.level_format, level)  +"      "+ levelTitles[level-1]
        binding.levelProgressBar.progress = user.score % 100
    }

    private fun setupClickListeners() {
        // Write card click listener
        binding.writeCard.setOnClickListener {
            animateCardClick(it)
            startActivity(Intent(this, WriteActivity::class.java))
        }

        // Analysis card click listener
        binding.analysisCard.setOnClickListener {
            animateCardClick(it)
            startActivity(Intent(this, CounselActivity::class.java))
        }

        // Journal card click listener
        binding.journalCard.setOnClickListener {
            animateCardClick(it)
            startActivity(Intent(this, DataActivity::class.java))
        }

        // Chat card click listener
        binding.chatCard.setOnClickListener {
            animateCardClick(it)
            startActivity(Intent(this, AIMenuActivity::class.java))
        }
    }

    private fun animateCardClick(view: View) {
        val scaleDown = AnimationUtils.loadAnimation(this, R.anim.card_click_scale_down)
        val scaleUp = AnimationUtils.loadAnimation(this, R.anim.card_click_scale_up)

        view.startAnimation(scaleDown)
        scaleDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                view.startAnimation(scaleUp)
            }
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
}