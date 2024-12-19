package com.example.letscouncil

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.letscouncil.data.entity.AnalysisResult
import com.example.letscouncil.data.entity.DiaryEntry
import com.example.letscouncil.data.model.AggregatedAnalysis
import com.example.letscouncil.databinding.ActivityCounselBinding
import com.example.letscouncil.viewmodel.DiaryViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first

class CounselActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCounselBinding
    private lateinit var viewModel: DiaryViewModel
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCounselBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[DiaryViewModel::class.java]

        setupToolbar()
        checkAndLoadAnalysis()
    }

    private fun checkAndLoadAnalysis() {
        lifecycleScope.launch {
            try {
                // 먼저 재분석 필요 여부 확인
                val needsReanalysis = viewModel.shouldReanalyze().first()

                if (needsReanalysis) {
                    // 새 분석 시작
                    showLoading(true)

                    // 최근 항목들 가져오기
                    viewModel.getRecentEntries(7).observe(this@CounselActivity) { entries ->
                        if (!entries.isNullOrEmpty()) {
                            lifecycleScope.launch {
                                try {
                                    // 분석 수행
                                    val analysis = analyzeEntries(entries)
                                    val analysisResult = createAnalysisResult(analysis)
                                    viewModel.saveAnalysisResult(analysisResult)

                                    // UI 업데이트
                                    withContext(Dispatchers.Main) {
                                        updateUI(analysisResult)
                                        showLoading(false)
                                    }
                                } catch (e: Exception) {
                                    Log.e("CounselActivity", "Analysis failed", e)
                                    withContext(Dispatchers.Main) {
                                        showLoading(false)
                                    }
                                }
                            }
                        } else {
                            showLoading(false)
                        }
                    }
                } else {
                    // 저장된 분석 결과 사용
                    viewModel.getLatestAnalysis().observe(this@CounselActivity) { analysis ->
                        analysis?.let {
                            updateUI(it)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CounselActivity", "Error in checkAndLoadAnalysis", e)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        Log.d("CounselActivity", "ShowLoading: $show")  // 디버깅용 로그
        binding.loadingLayout.visibility = if (show) View.VISIBLE else View.GONE
        binding.nestedScrollView.visibility = if (show) View.GONE else View.VISIBLE
    }
    private suspend fun analyzeEntries(entries: List<DiaryEntry>): AggregatedAnalysis {
        var totalPositive = 0f
        var totalNeutral = 0f
        var totalNegative = 0f
        val emotionKeywords = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        entries.forEach { entry ->
            val response = generativeModel.generateContent(
                """
            일기 내용의 감정을 수치화해서 분석해주세요.
            "${entry.content}"

            아래 형식으로 정확한 수치를 제시해주세요. 합이 100이 되어야 합니다:
            긍정적 감정: 숫자만%
            중립적 감정: 숫자만%
            부정적 감정: 숫자만%
            """.trimIndent()
            )

            Log.d("CounselActivity", "AI Response: ${response.text}")  // 응답 확인용 로그

            val keywordResponse = generativeModel.generateContent(
                """
            이 일기에서 느껴지는 주요 감정 3개를 추출해주세요:
            "${entry.content}"
            """.trimIndent()
            )
            emotionKeywords.add(keywordResponse.text ?: "")

            val percentages = extractPercentages(response.text ?: "")

            // 값 검증용 로그
            Log.d("CounselActivity", "Extracted percentages: positive=${percentages.first}, neutral=${percentages.second}, negative=${percentages.third}")

            totalPositive += percentages.first
            totalNeutral += percentages.second
            totalNegative += percentages.third
        }

        // 평균값 계산 및 검증용 로그
        val avgPositive = totalPositive / entries.size
        val avgNeutral = totalNeutral / entries.size
        val avgNegative = totalNegative / entries.size

        Log.d("CounselActivity", "Averages before normalization: positive=$avgPositive, neutral=$avgNeutral, negative=$avgNegative")

        // 합이 100이 되도록 정규화
        val sum = avgPositive + avgNeutral + avgNegative
        val normalizedPositive = if (sum > 0) (avgPositive / sum) * 100 else 0f
        val normalizedNeutral = if (sum > 0) (avgNeutral / sum) * 100 else 0f
        val normalizedNegative = if (sum > 0) (avgNegative / sum) * 100 else 0f

        // 정규화 후 값 검증용 로그
        Log.d("CounselActivity", "Final normalized values: positive=$normalizedPositive, neutral=$normalizedNeutral, negative=$normalizedNegative")

        val recommendationResponse = generativeModel.generateContent(
            "지금의 감정 상태를 고려하여 도움이 될 만한 활동 3가지를 추천해주세요."
        )
        recommendations.add(recommendationResponse.text ?: "")

        return AggregatedAnalysis(
            positivePercent = normalizedPositive,
            neutralPercent = normalizedNeutral,
            negativePercent = normalizedNegative,
            emotionKeywords = emotionKeywords,
            recommendations = recommendations
        )
    }

    // extractPercentages 함수도 수정
    private fun extractPercentages(text: String): Triple<Float, Float, Float> {
        try {
            val numbers = text.split("%")
                .mapNotNull { str ->
                    str.filter { it.isDigit() || it == '.' }
                        .toFloatOrNull()
                }
            Log.d("CounselActivity", "Extracted numbers from text: $numbers")

            return Triple(
                numbers.getOrNull(0) ?: 0f,
                numbers.getOrNull(1) ?: 0f,
                numbers.getOrNull(2) ?: 0f
            )
        } catch (e: Exception) {
            Log.e("CounselActivity", "Error parsing percentages", e)
            return Triple(0f, 0f, 0f)
        }
    }
    private fun createAnalysisResult(analysis: AggregatedAnalysis): AnalysisResult {
        return AnalysisResult(
            timestamp = System.currentTimeMillis(),
            positivePercent = analysis.positivePercent,
            neutralPercent = analysis.neutralPercent,
            negativePercent = analysis.negativePercent,
            emotionKeywords = JSONArray(analysis.emotionKeywords).toString(),
            recommendations = JSONArray(analysis.recommendations).toString(),
            lastAnalyzedDiaryDate = System.currentTimeMillis()
        )
    }

    private fun updateUI(analysis: AnalysisResult) {
        // 감정 키워드 표시
        val keywords = JSONArray(analysis.emotionKeywords)
        val keywordsList = (0 until keywords.length()).map { keywords.getString(it) }
        binding.tvAnalysisSummary.text = keywordsList.joinToString("\n")

        // 값들을 반올림하여 소수점 첫째자리까지 표시
        val positivePercent = (analysis.positivePercent * 10).roundToInt() / 10f
        val neutralPercent = (analysis.neutralPercent * 10).roundToInt() / 10f
        val negativePercent = (analysis.negativePercent * 10).roundToInt() / 10f

        // 프로그레스바와 텍스트 업데이트 - 반올림된 값 사용
        binding.emotion1.text = "긍정적 감정: ${String.format("%.1f", positivePercent)}%"
        binding.emotion2.text = "중립적 감정: ${String.format("%.1f", neutralPercent)}%"
        binding.emotion3.text = "부정적 감정: ${String.format("%.1f", negativePercent)}%"

        binding.progressPositive.progress = positivePercent.roundToInt()
        binding.progressNeutral.progress = neutralPercent.roundToInt()
        binding.progressNegative.progress = negativePercent.roundToInt()

        // 파이 차트 업데이트 - 동일한 반올림된 값 사용
        val entries = listOf(
            PieEntry(negativePercent / 100f, "부정적 감정"),
            PieEntry(neutralPercent / 100f, "중립적 감정"),
            PieEntry(positivePercent / 100f, "긍정적 감정")
        )

        val dataSet = PieDataSet(entries, "감정 분석 결과").apply {
            colors = listOf(Color.RED, Color.GRAY, Color.GREEN)
            valueTextColor = Color.BLACK
            valueTextSize = 14f
            valueFormatter = PercentFormatter()
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isRotationEnabled = true
            centerText = "일주일 감정 분포"
            setCenterTextSize(18f)
            animateY(1000)
            setUsePercentValues(true)
            invalidate()
        }

        // 추천 활동 표시
        val recommendations = JSONArray(analysis.recommendations)
        binding.RA.text = if (recommendations.length() > 0) recommendations.getString(0) else ""
    }

    private fun updatePieChart(analysis: AnalysisResult) {
        val entries = listOf(
            PieEntry(analysis.negativePercent, "부정적 감정"),
            PieEntry(analysis.neutralPercent, "중립적 감정"),
            PieEntry(analysis.positivePercent, "긍정적 감정")
        )

        val dataSet = PieDataSet(entries, "감정 분석 결과").apply {
            colors = listOf(Color.RED, Color.GRAY, Color.GREEN)
            valueTextColor = Color.BLACK
            valueTextSize = 14f
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isRotationEnabled = true
            centerText = "일주일 감정 분포"
            setCenterTextSize(18f)
            animateY(1000)
            invalidate()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarCounsel)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding.toolbarCounsel.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}