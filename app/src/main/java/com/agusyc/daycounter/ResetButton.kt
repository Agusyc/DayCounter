package com.agusyc.daycounter

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.AttributeSet

class ResetButton(context: Context, attrs: AttributeSet?) : android.support.v7.widget.AppCompatImageView(context, attrs) {
    private var widget_id: Int = 0
    private var isWidget: Boolean = false

    init {
        setOnClickListener {
            val prefs: SharedPreferences
            if (isWidget)
                prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)
            else
                prefs = context.getSharedPreferences("ListDaysPrefs", Context.MODE_PRIVATE)
            prefs.edit().putLong(widget_id.toString() + "date", System.currentTimeMillis()).apply()

            var updaterIntent: Intent
            if (isWidget) {
                updaterIntent = Intent(context, WidgetUpdater::class.java)
                updaterIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                updaterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widget_id))
                context.sendBroadcast(updaterIntent)
            }

            updaterIntent = Intent(context, CounterNotificator::class.java)
            updaterIntent.action = CounterNotificator.ACTION_UPDATE_NOTIFICATIONS
            updaterIntent.putExtra((if (isWidget) "widget_ids" else "list_ids"), intArrayOf(widget_id))
            context.sendBroadcast(updaterIntent)

            val activity = context as MainActivity
            activity.dontAnimate = false
            activity.updateListView()
        }
    }

    fun setWidgetID(ID: Int) {
        widget_id = ID
    }

    fun setIsWidget(isWidget: Boolean) {
        this.isWidget = isWidget
    }
}
