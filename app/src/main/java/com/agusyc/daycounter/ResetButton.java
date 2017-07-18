package com.agusyc.daycounter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class ResetButton extends android.support.v7.widget.AppCompatImageView {
    private long widget_id;

    public ResetButton(final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE);
                prefs.edit().putLong(widget_id + "date", System.currentTimeMillis()).apply();

                MainActivity activity = (MainActivity) context;
                activity.updateListView();
            }
        });
    }

    public void setWidgetID(long ID) {
        widget_id = ID;
    }
}
