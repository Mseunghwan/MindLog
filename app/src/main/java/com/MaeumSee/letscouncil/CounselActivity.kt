package com.min.mindlog

import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.MaeumSee.BuildConfig
import com.MaeumSee.R
import com.MaeumSee.databinding.ActivityCounselBinding
import com.min.mindlog.data.UserPreferences
import com.min.mindlog.data.entity.AnalysisResult
import com.min.mindlog.data.entity.DiaryEntry
import com.min.mindlog.data.model.AggregatedAnalysis
import com.min.mindlog.viewmodel.DiaryViewModel
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import kotlin.math.roundToInt

class CounselActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCounselBinding
    private lateinit var viewModel: DiaryViewModel
    private lateinit var userPreferences: UserPreferences
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        // APIKEY는 local.properties로 관리하여 유출을 방지합니다
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        // 액티비티 전환 애니메이션 설정
        window.exitTransition = TransitionInflater.from(this)
            .inflateTransition(R.transition.activity_fade)

        super.onCreate(savedInstanceState)
        binding = ActivityCounselBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // UserPreferences 초기화
        userPreferences = UserPreferences(this)

        // 사용자 정보 가져오기
        val user = userPreferences.getUser()

        // 사용자 이름, 연령, 직업 기반 메시지 생성
        val userName = user?.name ?: "사용자"
        val userAge = user?.birthYear?.let { calculateAge(it) } ?: "알 수 없음"
        val userOccupation = user?.occupation ?: "알 수 없음"

        viewModel = ViewModelProvider(this)[DiaryViewModel::class.java]

        setupToolbar()
        checkAndLoadAnalysis()
    }

    private fun calculateAge(birthYear: Int): Int {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        return currentYear - birthYear
    }

    private fun checkAndLoadAnalysis() {
        val user = userPreferences.getUser()
        val userName = user?.name ?: ""
        val userAge = user?.birthYear?.let { calculateAge(it) }
        val userOccupation = user?.occupation ?: "알 수 없음"
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
                                    val analysis = analyzeEntries(entries, userName, userAge, userOccupation)

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
    private suspend fun analyzeEntries(entries: List<DiaryEntry>, userName: String, userAge: Int?, userOccupation: String?): AggregatedAnalysis {
        var totalPositive = 0f
        var totalNeutral = 0f
        var totalNegative = 0f

        // 모든 일기 내용을 하나의 문자열로 결합
        val allContent = entries.joinToString("\n") { it.content }

        // 각 일기별 감정 수치 분석
        val emotionScores = entries.map { entry ->
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

            extractPercentages(response.text ?: "")
        }

        // 전체 일기에 대한 감정 키워드 한 번만 분석
        val keywordResponse = generativeModel.generateContent(
            """
    아래의 모든 일기들에서 느껴지는 주요 감정 5개만 추출해주세요. 
    각 감정은 한 단어로만 표현해주세요:
    
    "${allContent}"
    """.trimIndent()
        )

        val emotionKeywords = keywordResponse.text ?: ""

        // 감정 점수 합산
        emotionScores.forEach { (positive, neutral, negative) ->
            totalPositive += positive
            totalNeutral += neutral
            totalNegative += negative
        }

        // 평균 계산
        val avgPositive = totalPositive / entries.size
        val avgNeutral = totalNeutral / entries.size
        val avgNegative = totalNegative / entries.size

        // 정규화
        val sum = avgPositive + avgNeutral + avgNegative
        val normalizedPositive = if (sum > 0) (avgPositive / sum) * 100 else 0f
        val normalizedNeutral = if (sum > 0) (avgNeutral / sum) * 100 else 0f
        val normalizedNegative = if (sum > 0) (avgNegative / sum) * 100 else 0f

        // 추천사항 요청 - 감정 키워드를 명시적으로 포함
        val recommendationResponse = generativeModel.generateContent(
            """
        사용자 정보:
        이름: ${userName.ifEmpty { "사용자" }}
        나이: ${userAge ?: "알 수 없음"}
        직업: ${userOccupation ?: "알 수 없음"}
                
        다음과 같은 감정들이 관찰되었습니다: $emotionKeywords
        
        이러한 감정 상태를 고려했을 때, 사용자의 나이, 직업을 고려해 현재 상황에서 도움이 될 만한 구체적인 활동 3가지를 추천해주세요.
        각 활동은 현재의 감정 상태를 개선하거나 보완하는데 도움이 되어야 합니다.
        """.trimIndent()

        )

        return AggregatedAnalysis(
            positivePercent = normalizedPositive,
            neutralPercent = normalizedNeutral,
            negativePercent = normalizedNegative,
            emotionKeywords = listOf(emotionKeywords),
            recommendations = listOf(recommendationResponse.text ?: "")
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
        // 감정 점수 업데이트
        binding.mainEmotionScore.text = "${analysis.positivePercent.roundToInt()}%"
        binding.neutralScore.text = "${analysis.neutralPercent.roundToInt()}%"
        binding.negativeScore.text = "${analysis.negativePercent.roundToInt()}%"

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
    }

    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}