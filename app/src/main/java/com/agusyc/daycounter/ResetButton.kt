package com.agusyc.daycounter

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.AttributeSet
import org.joda.time.DateTime

class ResetButton(context: Context, attrs: AttributeSet?) : android.support.v7.widget.AppCompatImageView(context, attrs) {
    var widget_id: Int = 0
    var isWidget: Boolean = false

    init {
	// This click listener resets the counter's date
	setOnClickListener {
            val prefs: SharedPreferences
	    // We get the right preferences depeding on isWidget
            if (isWidget)
                prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)
            else
                prefs = context.getSharedPreferences("ListDaysPrefs", Context.MODE_PRIVATE)
            prefs.edit().putLong(widget_id.toString() + "date", DateTime.now().withTime(0, 0, 0, 0).millis).apply()

            var updaterIntent: Intent
	    // If the counter being resetted is a widget, we tell the WidgetUpdater to update it
            if (isWidget) {
                updaterIntent = Intent(context, WidgetUpdater::class.java)
                updaterIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                updaterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widget_id))
                context.sendBroadcast(updaterIntent)
            }

	    // We tell the notificator to update the counter
            updaterIntent = Intent(context, CounterNotificator::class.java)
            updaterIntent.action = CounterNotificator.ACTION_UPDATE_NOTIFICATIONS
            updaterIntent.putExtra((if (isWidget) "widget_ids" else "list_ids"), intArrayOf(widget_id))
            context.sendBroadcast(updaterIntent)

	    // We update the list view in the MainActivity
            val activity = context as MainActivity
            activity.dontAnimate = false
            activity.updateListView()
        }
    }
}
