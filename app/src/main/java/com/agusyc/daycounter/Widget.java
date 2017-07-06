package com.agusyc.daycounter;

import android.content.Context;
import android.content.SharedPreferences;

// This class represents a Widget
class Widget {
    private long id;
    private String label;
    private long date;
    private int color;
    private int color_index;

    // The constructor
    Widget(Context context, long id) {
        this.id = id;
        SharedPreferences prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE);
        label = prefs.getString(id + "label", null);
        date = prefs.getLong(id + "date", 0);
        color = prefs.getInt(id + "color", 0);
        color_index = prefs.getInt(id + "color_index", 0);
    }

    long getID() {
        return id;
    }

    String getLabel() {
        return label;
    }

    long getDate() {
        return date;
    }

    int getColor() {
        return color;
    }

    int getColorIndex() {
        return color_index;
    }
}