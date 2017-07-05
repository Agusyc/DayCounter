package com.agusyc.daycounter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
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

        setColorFilter(color);
        setImageResource(R.drawable.circle);
    }

    public int getColor() {
        return color;
    }
}


