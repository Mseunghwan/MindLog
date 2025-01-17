package com.example.letscouncil.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.letscouncil.AIMenuActivity
import com.example.letscouncil.CounselActivity
import com.example.letscouncil.DataActivity
import com.example.letscouncil.data.entity.DiaryEntry
import com.example.letscouncil.databinding.ItemDiaryBinding

class DiaryAdapter(private var diaryEntries: List<DiaryEntry>) :
    RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>() {

    // ViewHolder 클래스 정의
    class DiaryViewHolder(private val binding: ItemDiaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: DiaryEntry, onItemClick: (Int) -> Unit) {
            binding.dateText.text = formatDate(entry.date)
            binding.contentText.text = entry.content

            // 확장 상태에 따라 텍스트의 최대 줄 수 설정
            binding.contentText.maxLines = if (entry.isExpanded) Int.MAX_VALUE else 3
            binding.contentText.ellipsize = if (entry.isExpanded) null else android.text.TextUtils.TruncateAt.END

            // 클릭 이벤트 설정
            binding.textdata.setOnClickListener {
                onItemClick(adapterPosition) // 클릭된 아이템의 위치 전달
            }
        }


        // 날짜 형식 변환 함수
        private fun formatDate(timestamp: Long): String {
            val formatter = java.text.SimpleDateFormat("yyyy년 MM월 dd일", java.util.Locale.getDefault())
            return formatter.format(java.util.Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val binding = ItemDiaryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DiaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val entry = diaryEntries[position]
        holder.bind(entry) { clickedPosition ->
            // 확장 상태 토글
            diaryEntries[clickedPosition].isExpanded = !diaryEntries[clickedPosition].isExpanded
            notifyItemChanged(clickedPosition) // 변경된 아이템만 갱신
        }
    }

    override fun getItemCount(): Int = diaryEntries.size

    // 데이터 업데이트 함수
    fun updateEntries(newEntries: List<DiaryEntry>) {
        diaryEntries = newEntries
        notifyDataSetChanged()
    }
}
