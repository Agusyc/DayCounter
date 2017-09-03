package com.agusyc.daycounter

import android.content.Context
import android.content.SharedPreferences

// This class represents a Counter
internal class Counter
(context: Context, val id: Int, val isWidget: Boolean) {
    // We declare all the needed variables
    val label: String
    val date: Long
    val color: Int
    val colorIndex: Int
    val notification: Boolean

    init {
        // We get the right prefs file, according to the isWidget variable, and we parse all the needed data
        val prefs: SharedPreferences = if (isWidget) context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE) else context.getSharedPreferences("ListDaysPrefs", Context.MODE_PRIVATE)

        label = prefs.getString(id.toString() + "label", "")
        date = prefs.getLong(id.toString() + "date", 0)
        color = prefs.getInt(id.toString() + "color", 0)
        colorIndex = prefs.getInt(id.toString() + "color_index", 0)
        notification = prefs.getBoolean(id.toString() + "notification", false)
    }
}
