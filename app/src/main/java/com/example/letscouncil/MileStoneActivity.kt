package com.example.letscouncil

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.letscouncil.view.MilestoneItem
import com.example.letscouncil.view.MilestoneView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton

class MilestoneActivity : AppCompatActivity() {

    private lateinit var milestoneView: MilestoneView
    private lateinit var goalDetailInput: TextInputEditText
    private lateinit var saveGoalButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_milestone)

        // 뷰 초기화
        milestoneView = findViewById(R.id.milestoneView)
        goalDetailInput = findViewById(R.id.goalDetailInput)
        saveGoalButton = findViewById(R.id.saveGoalButton)

        // 샘플 데이터로 마일스톤 뷰 초기화
        val sampleMilestones = listOf(
            MilestoneItem(1, "첫 번째 목표", "설명", true),
            MilestoneItem(2, "두 번째 목표", "설명", false),
            MilestoneItem(3, "세 번째 목표", "설명", false)
        )
        milestoneView.setMilestones(sampleMilestones)

        // 목표 저장 버튼 클릭 리스너
        saveGoalButton.setOnClickListener {
            val goalDetail = goalDetailInput.text.toString()
            if (goalDetail.isNotEmpty()) {
                // TODO: 목표 저장 로직 구현
            }
        }
    }
}