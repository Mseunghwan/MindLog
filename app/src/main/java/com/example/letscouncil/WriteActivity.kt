package com.example.letscouncil

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.letscouncil.databinding.ActivityWriteBinding

class WriteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityWriteBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar1)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 누른 날짜를 제목으로 설정함. mainActivity에서 날짜를 이름으로 받음
        binding.date.text = intent.getStringExtra("Title Date")


        // 저장버튼
        binding.savebtn.setOnClickListener {
            //val returnIntent = Intent()
            //returnIntent.putExtra("Data", binding.editMessage.text.toString())
            //setResult(RESULT_OK, returnIntent)

            saveData()
            Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show()
            finish()
        }


        // 글자 입력에 따른 액션
        binding.editMessage.addTextChangedListener() {


        }

        val intent = Intent(this, AnalysisActivity::class.java)
        binding.databtn.setOnClickListener {
            startActivity(intent)
        }

    }


    // 뒤로버튼 누를 시 저장하시겠습니까? 팝업창 띄울 것
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }


    private fun saveData() {

        val binding = ActivityWriteBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar1)
        setContentView(binding.root)

        val pref = getSharedPreferences("pref", 0)
        val edit = pref.edit() // 수정 모드
        // 1번째 인자는 키, 2번째 인자는 실제 담아둘 값
        edit.putString("Data", binding.editMessage.text.toString())
        edit.apply() // 저장완료

    }

    fun loadData() {

        val binding = ActivityWriteBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar1)
        setContentView(binding.root)

        val pref = getSharedPreferences("pref", 0)
        // 1번째 인자는 키, 2번째 인자는 데이터가 존재하지 않을경우의 값
        binding.editMessage.setText(pref.getString("Data", ""))

    }
}