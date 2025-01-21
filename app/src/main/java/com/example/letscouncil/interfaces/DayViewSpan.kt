package com.example.letscouncil.interfaces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import com.prolificinteractive.materialcalendarview.CalendarDay

interface DayViewSpan {
    fun onDraw(
        canvas: Canvas,
        textBounds: Rect,
        date: CalendarDay,
        position: Int,
        context: Context
    )
}