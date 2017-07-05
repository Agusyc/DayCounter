package com.agusyc.daycounter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridLayout;

public class ColorImageView extends android.support.v7.widget.AppCompatImageView {

    public ColorImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ColorImageView,
                0, 0);

        int color;

        try {
            color = a.getColor(R.styleable.ColorImageView_picked_color, Color.BLACK);
        } finally {
            a.recycle();
        }

        setColorFilter(color);
        setImageResource(R.drawable.circle);

        ColorImageViewClickListener listener = new ColorImageViewClickListener(color);

        setOnClickListener(listener);
    }

    private class ColorImageViewClickListener implements OnClickListener {

        private int color;
        private GridLayout layout;

        ColorImageViewClickListener(int color) {
            this.color = color;
        }

        @Override
        public void onClick(View v) {
            System.out.println(getParent().getParent());
            ((View) getParent().getParent()).setBackgroundColor(color);
        }
    }
}


