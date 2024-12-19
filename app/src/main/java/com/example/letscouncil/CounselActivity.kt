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
            // 감정 분석 프롬프트 개선
            val response = generativeModel.generateContent(
                """
            다음 일기 내용을 정확하게 감정 분석해주세요:
            "${entry.content}"
            
            분석 방법:
            1. 일기에서 언급된 감정 단어들의 실제 출현 빈도를 세어주세요
            2. 각 감정 단어의 강도와 문맥을 고려해주세요
            3. 감정이 직접적으로 언급되지 않은 경우, 문장의 어조와 내용을 통해 파악해주세요
            
            감정 분류:
            - 긍정적 감정: 행복, 기쁨, 만족, 감사, 즐거움, 설렘, 기대감, 편안함 등
            - 중립적 감정: 평온, 담담함, 보통의 감정, 일상적인 상태
            - 부정적 감정: 슬픔, 분노, 불안, 우울, 걱정, 스트레스, 당황 등
            
            분석 시 주의사항:
            1. 감정 단어의 실제 출현 빈도를 가장 중요한 기준으로 삼으세요
            2. 같은 감정이 반복적으로 언급된 경우, 그만큼 높은 비중을 차지해야 합니다
            3. 긍정적 감정이 n번, 부정적 감정이 m번 언급되었다면, 그 비율이 결과에 확실히 반영되어야 합니다
            
            다음 형식으로 응답해주세요:
            긍정적 감정: XX%
            중립적 감정: XX%
            부정적 감정: XX%
            (비율의 총합은 100이 되어야 합니다)
            """
            )

            // 감정 키워드 추출도 더 정확하게
            val keywordResponse = generativeModel.generateContent(
                """
            다음 일기 내용에서 가장 자주 언급되거나 강하게 표현된 감정을 순서대로 3개의 키워드로 요약해주세요:
            "${entry.content}"
            
            주의사항:
            1. 실제 텍스트에서 가장 많이 언급된 감정을 우선적으로 선택하세요
            2. 감정 단어의 출현 빈도를 반영하세요
            3. 같은 감정이 여러 번 언급되었다면 그 감정을 우선 선택하세요
            
            응답 형식: "가장 많이 언급된 감정, 두 번째로 많이 언급된 감정, 세 번째로 많이 언급된 감정"
            """
            )
            emotionKeywords.add(keywordResponse.text ?: "")

            val percentages = extractPercentages(response.text ?: "")
            totalPositive += percentages.first
            totalNeutral += percentages.second
            totalNegative += percentages.third
        }

        // 평균 계산과 정규화는 동일하게 유지
        val avgPositive = totalPositive / entries.size
        val avgNeutral = totalNeutral / entries.size
        val avgNegative = totalNegative / entries.size

        val sum = avgPositive + avgNeutral + avgNegative
        val normalizedPositive = if (sum > 0) (avgPositive / sum) * 100 else 80f  // 기본값 변경
        val normalizedNeutral = if (sum > 0) (avgNeutral / sum) * 100 else 15f   // 기본값 변경
        val normalizedNegative = if (sum > 0) (avgNegative / sum) * 100 else 5f  // 기본값 변경

        // 추천 활동도 감정 빈도를 반영하도록 수정
        val recommendationResponse = generativeModel.generateContent(
            """
        감정 분석 결과, 다음과 같은 감정 상태를 보이고 있습니다:
        긍정: ${normalizedPositive}%
        중립: ${normalizedNeutral}%
        부정: ${normalizedNegative}%

        현재 우세한 감정을 고려하여:
        1. 현재의 감정이 긍정적이라면 더욱 강화하고 지속시킬 수 있는 활동
        2. 이러한 긍정적 상태를 다른 영역으로도 확장할 수 있는 활동
        3. 부정적 감정을 자연스럽게 해소할 수 있는 활동

        위 관점에서 구체적이고 실천 가능한 활동 3가지를 추천해주세요.
        각 활동은 현재의 감정 상태를 고려하여 제안해주세요.
        """
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

    private fun extractPercentages(text: String): Triple<Float, Float, Float> {
        val numbers = text.split("%")
            .mapNotNull { it.findLast { char -> char.isDigit() }?.toString()?.toFloatOrNull() }
        return Triple(
            numbers.getOrNull(0) ?: 0f,
            numbers.getOrNull(1) ?: 0f,
            numbers.getOrNull(2) ?: 0f
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