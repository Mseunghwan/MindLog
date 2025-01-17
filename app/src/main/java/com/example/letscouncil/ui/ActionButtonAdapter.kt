package com.example.letscouncil.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.letscouncil.R
import com.example.letscouncil.databinding.ActionButtonItemBinding

class ActionButtonAdapter(
    private val actions: List<ActionButton>,
    private val onActionClick: (ActionButton) -> Unit
) : RecyclerView.Adapter<ActionButtonAdapter.ActionButtonViewHolder>() {

    inner class ActionButtonViewHolder(
        private val binding: ActionButtonItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(action: ActionButton) {
            binding.apply {
                actionIcon.setImageResource(action.iconResId)
                actionTitle.text = action.title
                actionDescription.text = action.description

                // 터치 이벤트 리스너 설정
                root.setOnClickListener {
                    // 클릭 애니메이션 재생 후 클릭 이벤트 실행
                    val scaleDown = AnimationUtils.loadAnimation(root.context, R.anim.card_click_scale_down)
                    root.startAnimation(scaleDown)
                    scaleDown.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {
                            onActionClick(action)
                        }
                        override fun onAnimationStart(animation: Animation?) {}
                        override fun onAnimationRepeat(animation: Animation?) {}
                    })
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionButtonViewHolder {
        val binding = ActionButtonItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActionButtonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActionButtonViewHolder, position: Int) {
        holder.bind(actions[position])
    }

    override fun getItemCount(): Int = actions.size
}