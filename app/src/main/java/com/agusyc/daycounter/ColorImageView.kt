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

    internal var color: Int = 0

    // The constructor
    init {
        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ColorImageView,
                0, 0)

        // We set the color variable to the one defined in the XML file of the configuration activity
        try {
            color = a.getColor(R.styleable.ColorImageView_picked_color, Color.BLACK)
        } finally {
            // We finalise the attributes, so they don't waste memory
            a.recycle()
        }

        // We get the circle variable and paint over it with the proper color
        val circle = ContextCompat.getDrawable(context, R.drawable.circle)
        circle.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)

        setImageDrawable(circle)
    }

    companion object {
        // This function overlays two drawables together
        fun getOverlay(d1: Drawable, d2: Drawable): LayerDrawable {
            return LayerDrawable(arrayOf(d1, d2))
        }
    }
}


