package com.example.letscouncil

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.letscouncil.databinding.ActivityCounselBinding
import com.example.letscouncil.feature.chat.ChatViewModel
import com.example.letscouncil.viewmodel.DiaryViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking




class CounselActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCounselBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCounselBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar with back button
        setupToolbar()

        // Set up initial analysis data
        displayAnalysisData()
    }


    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarCounsel)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        // Handle back button click
        binding.toolbarCounsel.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun displayAnalysisData() {
        // 예시 데이터를 표시합니다
        // 실제로는 이전 화면에서 전달받은 데이터나 API 결과를 사용해야 합니다

        val WriteData = intent.getStringExtra("content")

        if(WriteData == null) {
            // 빈페이지 컨텐츠 뷰 설정
            return
        }

        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.apiKey
        )

        lifecycleScope.launch {

            // prompt
            val response1 = generativeModel.generateContent(WriteData.toString() + "\n 감정을 키워드로 3줄로 요약해줘")
            val response2 = generativeModel.generateContent(WriteData.toString() + "\n 긍정적 감정, 중립적 감정, 부정적 감정 을 키워드로 현재 상태를 % 로 나타내줘")
            val response3 = generativeModel.generateContent(WriteData.toString() + "\n 이러한 감정을 개선하기 위한 활동을 3가지를 각각 3줄 요약해서 추천해줘")

            // gemini가 주제를 강조하려고 쓰는 ** 을 이용해서 분리
            val res2 = response2.text?.split("**")


            // "000 감정: 00%" 형식 추출
            val positive_emotion = res2?.get(1) ?: toString()
            val neutral_emotion = res2?.get(3) ?: toString()
            val negative_emotion = res2?.get(5) ?: toString()

            // 퍼센트 수치만 추출
            val posp = positive_emotion.split(" ")[2].replace("%","").toInt()
            val neup = neutral_emotion.split(" ")[2].replace("%","").toInt()
            val negp = negative_emotion.split(" ")[2].replace("%","").toInt()


            // text 넣기
            binding.tvAnalysisSummary.text = response1.text
            binding.emotion1.text = positive_emotion
            binding.emotion2.text = neutral_emotion
            binding.emotion3.text = negative_emotion
            binding.RA.text = response3.text?.replace("**","")

            // ProgressBar 설정
            findViewById<ProgressBar>(R.id.progress_positive)?.progress = posp
            findViewById<ProgressBar>(R.id.progress_neutral)?.progress = neup
            findViewById<ProgressBar>(R.id.progress_negative)?.progress = negp

            // 원형 그래프
            val pieChart = findViewById<PieChart>(R.id.pieChart)
            // 데이터 입력
            val entries = listOf(
                PieEntry(negp.toFloat(), "부정적 감정"),
                PieEntry(neup.toFloat(), "중립적 감정"),
                PieEntry(posp.toFloat(), "긍정적 감정")
            )

            // 데이터셋 스타일 설정
            val dataSet = PieDataSet(entries, "감정 분석 결과").apply {
                colors = listOf(Color.RED, Color.GRAY, Color.GREEN)
                valueTextColor = Color.BLACK
                valueTextSize = 14f
            }

            // 데이터 연결
            val data = PieData(dataSet)
            pieChart.data = data

            // 파이 차트 스타일 설정
            pieChart.apply {
                description.isEnabled = false
                isRotationEnabled = true
                centerText = "감정 분포"
                setCenterTextSize(18f)
                animateY(1000) // 애니메이션 효과
            }

            pieChart.invalidate() // 그래프 새로고침
        }





    }



    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}