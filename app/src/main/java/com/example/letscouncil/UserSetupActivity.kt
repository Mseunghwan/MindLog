package com.example.letscouncil

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.Settings.Global.getString
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.DialogFragment
import com.example.letscouncil.data.UserPreferences
import com.example.letscouncil.data.model.User
import com.example.letscouncil.databinding.ActivityUserSetupBinding
import com.example.letscouncil.databinding.DialogLayoutBinding
import okhttp3.internal.wait
import java.util.Calendar

class UserSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티 전환 애니메이션 설정
        window.exitTransition = TransitionInflater.from(this)
            .inflateTransition(R.transition.activity_fade)
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

        val dialog = CustomDialog()
        dialog.show(supportFragmentManager, "CustomDialog")

        // 저장 버튼 클릭 리스너
        binding.saveButton.setOnClickListener {
            val name = binding.inputName.text.toString()
            val birthYear = years[binding.spinnerYear.selectedItemPosition]
            val birthMonth = months[binding.spinnerMonth.selectedItemPosition]
            val birthDay = days[binding.spinnerDay.selectedItemPosition]
            val occupation = occupations[binding.spinnerOccupation.selectedItemPosition]
            val score = 0

            if (name.isNotBlank()) {
                val user = User(name, birthYear, birthMonth, birthDay, occupation, score)
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
class CustomDialog : DialogFragment() {
    // 첫 시작 시 시작 축하 다이어로그
    private var _binding: DialogLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogLayoutBinding.inflate(inflater, container, false)
        val view = binding.root
        dialog?.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        binding.dialBtn1.setOnClickListener {
            dismiss()    // 대화상자를 닫는 함수
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}