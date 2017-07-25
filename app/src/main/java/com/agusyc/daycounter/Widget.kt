package com.agusyc.daycounter

import android.content.Context

// This class represents a Widget
internal class Widget// The constructor
(context: Context, val id: Int) {
    val label: String
    val date: Long
    val color: Int
    val colorIndex: Int

    init {
        val prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)
        label = prefs.getString(id.toString() + "label", null)
        date = prefs.getLong(id.toString() + "date", 0)
        color = prefs.getInt(id.toString() + "color", 0)
        colorIndex = prefs.getInt(id.toString() + "color_index", 0)
    }
}