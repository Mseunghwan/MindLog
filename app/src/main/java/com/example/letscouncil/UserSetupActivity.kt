package com.example.letscouncil

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.letscouncil.data.UserPreferences
import com.example.letscouncil.data.model.User
import com.example.letscouncil.databinding.ActivityUserSetupBinding
import java.util.Calendar

class UserSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityUserSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userPreferences = UserPreferences(this)

        // 오늘 날짜 가져오기
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH는 0부터 시작
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Spinner 데이터 설정
        val years = (1900..currentYear).toList()
        val months = (1..12).toList()
        val days = (1..31).toList()
        val occupations = listOf("학생", "아동", "직장인", "주부", "기타")

        // Adapter 설정
        binding.spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        binding.spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        binding.spinnerDay.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days)
        binding.spinnerOccupation.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, occupations)

        // 초기값 설정
        binding.spinnerYear.setSelection(years.indexOf(currentYear))
        binding.spinnerMonth.setSelection(months.indexOf(currentMonth))
        binding.spinnerDay.setSelection(days.indexOf(currentDay))

        // 저장 버튼 클릭 리스너
        binding.saveButton.setOnClickListener {
            val name = binding.inputName.text.toString()
            val birthYear = years[binding.spinnerYear.selectedItemPosition]
            val birthMonth = months[binding.spinnerMonth.selectedItemPosition]
            val birthDay = days[binding.spinnerDay.selectedItemPosition]
            val occupation = occupations[binding.spinnerOccupation.selectedItemPosition]

            if (name.isNotBlank()) {
                val user = User(name, birthYear, birthMonth, birthDay, occupation)
                userPreferences.saveUser(user)

                // MainActivity로 이동
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "이름을 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
