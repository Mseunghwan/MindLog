package com.example.letscouncil

import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letscouncil.adapter.DiaryAdapter
import com.example.letscouncil.data.database.DiaryDatabase
import com.example.letscouncil.data.entity.DiaryEntry
import com.example.letscouncil.databinding.ActivityDataBinding
import com.example.letscouncil.databinding.ItemDiaryBinding
import com.example.letscouncil.viewmodel.DiaryViewModel

class DataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataBinding
    private lateinit var viewModel: DiaryViewModel
    private lateinit var diaryAdapter: DiaryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        // 액티비티 전환 애니메이션 설정
        window.exitTransition = TransitionInflater.from(this)
            .inflateTransition(R.transition.activity_fade)

        super.onCreate(savedInstanceState)
        binding = ActivityDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar 설정
        setupToolbar()

        // ViewModelProvider로 ViewModel 초기화
        viewModel = ViewModelProvider(this)[DiaryViewModel::class.java]

        // RecyclerView 설정
        setupRecyclerView()

        // 데이터 가져오기
        viewModel.getAllEntries().observe(this) { entries ->
            if (entries == null || entries.isEmpty()) {
                Log.d("DataActivity", "No entries found")
                // 빈 상태 UI 업데이트
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateText.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.emptyStateText.visibility = View.GONE
                diaryAdapter.updateEntries(entries)
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar) // XML에 정의된 Toolbar를 앱바로 설정
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
            setDisplayShowHomeEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        diaryAdapter = DiaryAdapter(emptyList())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DataActivity)
            adapter = diaryAdapter
        }
    }
}
