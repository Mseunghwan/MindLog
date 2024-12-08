package com.example.letscouncil

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.letscouncil.databinding.ActivityWriteBinding
import com.example.letscouncil.viewmodel.DiaryViewModel
import java.util.Calendar

class WriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWriteBinding
    private lateinit var viewModel: DiaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
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

        setSupportActionBar(binding.toolbar1)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 버튼 설정
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
        val content = binding.editMessage.text.toString()
        if (content.isNotBlank() && content != "글을 입력해주세요") {
            viewModel.saveDiary(content)
            Toast.makeText(this, "일기가 저장되었습니다.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}
