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

        val pieChart = findViewById<PieChart>(R.id.pieChart)
        // 데이터 입력
        val entries = listOf(
            PieEntry(10f, "부정적 감정"),
            PieEntry(15f, "중립적 감정"),
            PieEntry(75f, "긍정적 감정")
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
        //CounselViewModel.setWriteData(WriteData)

        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.apiKey
        )

        lifecycleScope.launch {
            val response = generativeModel.generateContent(WriteData.toString())

            // 로딩바 만들기(미완)
            binding.progressbarAnalysis.visibility = View.VISIBLE
            binding.progressbarAnalysis.visibility = View.GONE


            // 요약해서 전송하고 싶은데.........................
            // ㅠㅠㅠㅠ
            binding.tvAnalysisSummary.text = response.text

        }

        // ProgressBar 설정
        findViewById<ProgressBar>(R.id.progress_positive)?.progress = 75
        findViewById<ProgressBar>(R.id.progress_neutral)?.progress = 15
        findViewById<ProgressBar>(R.id.progress_negative)?.progress = 10
    }



    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}