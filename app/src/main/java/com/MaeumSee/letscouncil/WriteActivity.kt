package com.min.mindlog

import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.MaeumSee.R
import com.MaeumSee.databinding.ActivityWriteBinding
import com.min.mindlog.data.UserPreferences
import com.min.mindlog.viewmodel.DiaryViewModel
import java.util.Calendar

class WriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWriteBinding
    private lateinit var viewModel: DiaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        // 액티비티 전환 애니메이션 설정
        window.exitTransition = TransitionInflater.from(this)
            .inflateTransition(R.transition.activity_fade)
        super.onCreate(savedInstanceState)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // ViewModel 초기화
        try {
            viewModel = ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            )[DiaryViewModel::class.java]
            Log.d("WriteActivity", "ViewModel initialized successfully")
        } catch (e: Exception) {
            Log.e("WriteActivity", "ViewModel initialization failed: ${e.message}", e)
        }

        // 뒤로가기 버튼 클릭 리스너 설정
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 저장 버튼 설정
        binding.savebtn.setOnClickListener { saveToDatabase() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // 뒤로가기 버튼의 기본 ID
                onBackPressedDispatcher.onBackPressed() // 뒤로가기 동작 호출
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveToDatabase() {
        val userPreferences = UserPreferences(this)
        val user = userPreferences.getUser()
        val content = binding.editMessage.text.toString()

        if (content.isNotBlank() && content != "글을 입력해주세요") {
            viewModel.saveDiary(content)

            // 현재 날짜의 시작시간(0시 0분 0초)을 가져옴
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val todayStart = calendar.timeInMillis

            // 마지막 작성 날짜와 비교
            val lastWrittenDate = userPreferences.getLastWrittenDate()

            if (lastWrittenDate < todayStart) {
                // 오늘 처음 작성하는 경우
                if (user != null) {
                    user.score += 20
                    userPreferences.saveUser(user)
                }
                userPreferences.setLastWrittenDate(System.currentTimeMillis())
                Toast.makeText(this, "일기가 저장되었습니다. +20 포인트를 획득했어요!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "일기가 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }

            finish()
        } else {
            Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}
