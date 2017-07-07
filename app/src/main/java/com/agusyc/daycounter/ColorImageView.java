package com.agusyc.daycounter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

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

        setImageBitmap(getCircle(context, getColor()));
    }

    public int getColor() {
        return color;
    }

    public static Bitmap getCircle(Context context, int color) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        final int radius = Math.round(60 * (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        Bitmap bmp = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        paint.setColor(color);

        Canvas canvas = new Canvas(bmp);
        canvas.drawCircle(radius / 2, radius / 2, radius / 2, paint);

        return bmp;
    }

    public static LayerDrawable getOverlay(Context context, Bitmap bmp1, Drawable d2) {
        BitmapDrawable bd1 = new BitmapDrawable(context.getResources(), bmp1);
        return new LayerDrawable(new Drawable[]{bd1, d2});
    }
}


