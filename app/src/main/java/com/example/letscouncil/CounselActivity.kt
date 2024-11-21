package com.example.letscouncil

import android.graphics.Color
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.letscouncil.databinding.ActivityCounselBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry


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
        binding.tvAnalysisSummary.text = "긍정적인 감정이 75%로 나타났습니다."

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