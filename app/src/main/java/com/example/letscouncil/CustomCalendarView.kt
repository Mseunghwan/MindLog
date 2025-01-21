package com.example.letscouncil

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.letscouncil.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.example.letscouncil.feature.chat.ChatViewModel
import java.util.Calendar

class CustomCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : MaterialCalendarView(context, attrs) {

    init {
        // 기본 설정
        state().edit()
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit()

        // 데코레이터 추가
        addDecorator(MoodDecorator())
    }

    inner class MoodDecorator : DayViewDecorator {
        override fun shouldDecorate(calendarDay: CalendarDay): Boolean = true

        override fun decorate(view: DayViewFacade) {
            try {
                val customView = LayoutInflater.from(context)
                    .inflate(R.layout.custom_calendar_view, null)

                // 현재 날짜의 캘린더 인스턴스 가져오기
                val currentDate = CalendarDay.today()

                customView.findViewById<TextView>(R.id.dayNumber)?.apply {
                    text = currentDate.day.toString()

                }

                // mood 이미지 설정 (예시)
                customView.findViewById<ImageView>(R.id.moodEmoji)?.apply {
                    visibility = View.VISIBLE
                    setImageResource(R.drawable.ic_mood) // 기본 이미지
                }

                // 뷰에 배경 적용
                customView.background?.let { view.setBackgroundDrawable(it) }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 날짜별 감정 상태를 설정하는 메서드
    fun setMoodForDate(date: Calendar, mood: ChatViewModel.Mood) {
        addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(calendarDay: CalendarDay): Boolean {
                return CalendarDay.from(date) == calendarDay
            }

            override fun decorate(view: DayViewFacade) {
                // 해당 날짜의 감정 상태에 따른 이모지 설정
                val moodEmoji = when (mood) {
                    ChatViewModel.Mood.VERY_HAPPY -> R.drawable.emoji_very_happy
                    ChatViewModel.Mood.HAPPY -> R.drawable.emoji_very_happy
                    ChatViewModel.Mood.NEUTRAL -> R.drawable.emoji_very_happy
                    ChatViewModel.Mood.SAD -> R.drawable.emoji_very_happy
                    ChatViewModel.Mood.ANXIOUS -> R.drawable.emoji_very_happy
                    ChatViewModel.Mood.ANGRY -> R.drawable.emoji_very_happy
                }

                val customView = LayoutInflater.from(context)
                    .inflate(R.layout.custom_calendar_view, null)

                customView.findViewById<ImageView>(R.id.moodEmoji)?.apply {
                    setImageResource(moodEmoji)
                    visibility = View.VISIBLE
                }

                customView.background?.let { view.setBackgroundDrawable(it) }
            }
        })
    }
}