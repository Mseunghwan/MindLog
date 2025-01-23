package com.min.mindlog

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.MaeumSee.databinding.ActivityDataBinding
import com.min.mindlog.adapter.DiaryAdapter
import com.min.mindlog.data.UserPreferences
import com.min.mindlog.viewmodel.DiaryViewModel

class DataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataBinding
    private lateinit var viewModel: DiaryViewModel
    private lateinit var diaryAdapter: DiaryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 버튼 클릭 리스너 설정
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // ViewModelProvider로 ViewModel 초기화
        viewModel = ViewModelProvider(this)[DiaryViewModel::class.java]

        // RecyclerView 설정
        setupRecyclerView()

        // 현재 월의 일기 개수 관찰
        viewModel.getCurrentMonthEntriesCount().observe(this) { count ->
            binding.monthlyCount.text = "${count}개"
        }

        // 전체 일기 데이터 관찰
        viewModel.getAllEntries().observe(this) { entries ->
            if (entries == null || entries.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateText.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.emptyStateText.visibility = View.GONE
                diaryAdapter.updateEntries(entries)
            }
        }
    }
    // DataActivity.kt
    private fun setupRecyclerView() {
        val userPreferences = UserPreferences(this)
        diaryAdapter = DiaryAdapter(emptyList(), userPreferences)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DataActivity)
            adapter = diaryAdapter
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }
}
