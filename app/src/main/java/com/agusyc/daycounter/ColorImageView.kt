package com.agusyc.daycounter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet

class ColorImageView(context: Context, attrs: AttributeSet?) : android.support.v7.widget.AppCompatImageView(context, attrs) {

    var color: Int = 0
        private set

    init {
        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ColorImageView,
                0, 0)

        try {
            color = a.getColor(R.styleable.ColorImageView_picked_color, Color.BLACK)
        } finally {
            a.recycle()
        }

        val circle = ContextCompat.getDrawable(context, R.drawable.circle)

        circle.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)

        setImageDrawable(circle)
    }

    companion object {

        fun getOverlay(d1: Drawable, d2: Drawable): LayerDrawable {
            return LayerDrawable(arrayOf(d1, d2))
        }
    }
}


