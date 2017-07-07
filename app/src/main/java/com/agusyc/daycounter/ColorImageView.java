package com.agusyc.daycounter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

public class ColorImageView extends android.support.v7.widget.AppCompatImageView {

    private int color;

    public ColorImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ColorImageView,
                0, 0);

        try {
            color = a.getColor(R.styleable.ColorImageView_picked_color, Color.BLACK);
        } finally {
            a.recycle();
        }

        Drawable circle = ContextCompat.getDrawable(context, R.drawable.circle);

        circle.setColorFilter(new
                PorterDuffColorFilter(getColor(), PorterDuff.Mode.SRC_ATOP));

        setImageDrawable(circle);
    }

    public int getColor() {
        return color;
    }

    public static LayerDrawable getOverlay(Drawable d1, Drawable d2) {
        return new LayerDrawable(new Drawable[]{d1, d2});
    }
}


