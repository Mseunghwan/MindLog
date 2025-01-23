package com.min.maeumsee.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.MaeumSee.databinding.ItemDiaryBinding
import com.MaeumSee.databinding.ItemDiaryHeaderBinding
import com.min.maeumsee.data.UserPreferences
import com.min.maeumsee.data.entity.DiaryEntry
import java.util.*

class DiaryAdapter(
    private var diaryEntries: List<DiaryEntry>,
    private val userPreferences: UserPreferences
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    inner class HeaderViewHolder(private val binding: ItemDiaryHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val user = userPreferences.getUser()
            binding.headerText.text = "${user?.name ?: "나"}의 일기 시작"
            binding.headerSubText.text = "지금부터 당신의 이야기가 시작됩니다"
        }
    }

    inner class DiaryViewHolder(private val binding: ItemDiaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: DiaryEntry) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = entry.date
            }

            binding.dayText.text = calendar.get(Calendar.DAY_OF_MONTH).toString()
            binding.monthText.text = "${calendar.get(Calendar.MONTH) + 1}월"

            binding.contentText.text = entry.content
            binding.contentText.maxLines = if (entry.isExpanded) Int.MAX_VALUE else 4
            binding.contentText.ellipsize = if (entry.isExpanded) null else android.text.TextUtils.TruncateAt.END

            binding.textdata.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION && pos < diaryEntries.size) {
                    diaryEntries[pos].isExpanded = !diaryEntries[pos].isExpanded
                    notifyItemChanged(pos)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemDiaryHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemDiaryBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                DiaryViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind()
            is DiaryViewHolder -> {
                if (position < diaryEntries.size) {  // 안전하게 체크
                    val entry = diaryEntries[position]
                    holder.bind(entry)
                }
            }
        }
    }

    override fun getItemCount(): Int = diaryEntries.size + 1 // 헤더 포함

    fun updateEntries(newEntries: List<DiaryEntry>) {
        diaryEntries = newEntries
        notifyDataSetChanged()
    }
}