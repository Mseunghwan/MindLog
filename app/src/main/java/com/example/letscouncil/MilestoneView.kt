package com.example.letscouncil.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.letscouncil.R
import com.google.android.material.card.MaterialCardView

class MilestoneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val recyclerView: RecyclerView
    private val adapter = MilestoneAdapter()

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.milestone_view, this, true)
        recyclerView = view.findViewById(R.id.milestonesRecyclerView)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = this@MilestoneView.adapter
        }
    }

    fun setMilestones(milestones: List<MilestoneItem>) {
        adapter.submitList(milestones)
    }
}

data class MilestoneItem(
    val id: Long,
    val title: String,
    val description: String,
    val isCompleted: Boolean
)

class MilestoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val milestoneIcon: ImageView = itemView.findViewById(R.id.milestoneIcon)

    fun bind(item: MilestoneItem) {
        // 완료된 마일스톤과 미완료된 마일스톤의 시각적 차이를 표현
        milestoneIcon.alpha = if (item.isCompleted) 1.0f else 0.5f

        itemView.setOnClickListener {
            // TODO: 클릭 이벤트 처리 (목표 상세 보기 등)
        }
    }
}

class MilestoneAdapter : RecyclerView.Adapter<MilestoneViewHolder>() {
    private var items = listOf<MilestoneItem>()

    fun submitList(newList: List<MilestoneItem>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MilestoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_milestone, parent, false)
        return MilestoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: MilestoneViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}