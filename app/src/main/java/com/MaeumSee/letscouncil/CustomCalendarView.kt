package com.MaeumSee.letscouncil

import android.content.Context
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.MaeumSee.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.CalendarMode
import java.util.Calendar

class CustomCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : MaterialCalendarView(context, attrs) {

    init {
        // 기본 설정
        state().edit()
            .setFirstDayOfWeek(Calendar.SUNDAY)
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit()

        // 데코레이터 추가
        addDecorators(
            TodayDecorator(context),
            CurrentMonthDecorator(),
            OtherMonthDecorator()
        )
    }

    // 오늘 날짜 데코레이터
    inner class TodayDecorator(context: Context) : DayViewDecorator {
        private val today = CalendarDay.today()
        private val accentBlue = ContextCompat.getColor(context, R.color.accent_blue)

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return day == today
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(ForegroundColorSpan(accentBlue))
            view.addSpan(RelativeSizeSpan(1.2f))
        }
    }

    // 현재 달 날짜 데코레이터
    inner class CurrentMonthDecorator : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return day.month == CalendarDay.today().month
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(ForegroundColorSpan(Color.BLACK))
        }
    }

    // 이전/다음 달 날짜 데코레이터
    inner class OtherMonthDecorator : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return day.month != CalendarDay.today().month
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(ForegroundColorSpan(Color.LTGRAY))
        }
    }
}