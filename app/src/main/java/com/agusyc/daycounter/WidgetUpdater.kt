package com.agusyc.daycounter

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import org.joda.time.DateTime
import org.joda.time.Days
import java.util.*

class WidgetUpdater : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // The receiver caught a broadcast:
        Log.d("WidgetUpdater", "Broadcast received!")


        Log.d("WidgetUpdater", "The action is: " + intent.action!!)
        // We check if the broadcast is telling us that a widget has been deleted
        if ("android.appwidget.action.APPWIDGET_DELETED" == intent.action) {
            // We call the deleteWidget method, that deletes the widget from the prefs
            deleteWidget(context, intent.extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID))
        } else if (intent.action != null && intent.action!!.startsWith(WIDGET_BUTTON)) {
            val id_s = intent.action!!.replace(WIDGET_BUTTON, "")

            Log.d("WidgetUpdater", "The reset counter button was clicked for id " + id_s)

            // Here, the button was clicked
            val prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)

            prefs.edit().putLong(id_s + "date", System.currentTimeMillis()).apply()
            onUpdate(context, AppWidgetManager.getInstance(context), intArrayOf(Integer.parseInt(id_s)))
        } else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == intent.action) {
            Log.d("WidgetUpdater", "Updating widgets...")
            val ids = intent.extras!!.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            // We update the widgets
            this.onUpdate(context, AppWidgetManager.getInstance(context), ids)
        }
    }

    private fun getPendingSelfIntent(context: Context, action: String, id: Int): PendingIntent {
        val intent = Intent(context, javaClass)
        intent.action = action + id
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    private fun deleteWidget(context: Context, id: Int) {
        Log.d("MainActivity", "Removing widget with id " + id)

        // We remove the label and date from the preferences
        val prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)
        prefs.edit().remove(id.toString() + "label").apply()
        prefs.edit().remove(id.toString() + "date").apply()
        prefs.edit().remove(id.toString() + "color").apply()
        prefs.edit().remove(id.toString() + "color_index").apply()

        val ids_set = prefs.getStringSet("ids", HashSet<String>())

        // We iterate trough the whole set, when we find the widget that is being deleted, we delete it from the set without mercy!
        val iterator = ids_set!!.iterator()
        while (iterator.hasNext()) {
            if (iterator.next() == Integer.toString(id)) {
                Log.d("WidgetUpdater", "Removing widget ID from preferences")
                iterator.remove()
                break
            }
        }

        // We put the new set to the prefs
        prefs.edit().putStringSet("ids", ids_set).apply()
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d("WidgetUpdater", "WidgetUpdater onUpdate started")

        val prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (appWidgetId in appWidgetIds) {

            // We get all the views that are in the widget
            val views = RemoteViews(context.packageName, R.layout.daycounter)

            // We get all the needed data
            val key_base = Integer.toString(appWidgetId)
            val label = prefs.getString(key_base + "label", "")
            val date = prefs.getLong(key_base + "date", 0)
            val currentTime = System.currentTimeMillis()

            // We use the Joda-Time method to calculate the difference
            val difference = Days.daysBetween(DateTime(date), DateTime(currentTime)).days

            // We set the days text to the *absolute* difference
            views.setTextViewText(R.id.txtDays, Integer.toString(Math.abs(difference)))

            // We check the sign of the number (Positive or negative). So we know if we use "since" or "until"
            if (difference > 0) {
                views.setTextViewText(R.id.txtThereAreHaveBeen, context.getString(R.string.there_have_been))
                views.setTextViewText(R.id.txtLabel, context.getString(R.string.days_since, label))

                views.setOnClickPendingIntent(R.id.btnReset, getPendingSelfIntent(context, WIDGET_BUTTON, appWidgetId))

                views.setViewVisibility(R.id.txtNoDays, View.GONE)
                views.setViewVisibility(R.id.btnReset, View.VISIBLE)
                views.setViewVisibility(R.id.divider, View.VISIBLE)
                views.setViewVisibility(R.id.txtDays, View.VISIBLE)
                views.setViewVisibility(R.id.txtThereAreHaveBeen, View.VISIBLE)
                views.setViewVisibility(R.id.txtLabel, View.VISIBLE)
            } else if (difference < 0) {
                views.setTextViewText(R.id.txtThereAreHaveBeen, context.getString(R.string.there_are))
                views.setTextViewText(R.id.txtLabel, context.getString(R.string.days_until, label))

                views.setViewVisibility(R.id.txtNoDays, View.GONE)
                views.setViewVisibility(R.id.btnReset, View.GONE)
                views.setViewVisibility(R.id.divider, View.GONE)
                views.setViewVisibility(R.id.txtDays, View.VISIBLE)
                views.setViewVisibility(R.id.txtThereAreHaveBeen, View.VISIBLE)
                views.setViewVisibility(R.id.txtLabel, View.VISIBLE)
            } else {
                views.setTextViewText(R.id.txtNoDays, context.getString(R.string.there_are_no_days_since, label))

                views.setViewVisibility(R.id.txtNoDays, View.VISIBLE)
                views.setViewVisibility(R.id.btnReset, View.GONE)
                views.setViewVisibility(R.id.txtDays, View.GONE)
                views.setViewVisibility(R.id.txtThereAreHaveBeen, View.GONE)
                views.setViewVisibility(R.id.txtLabel, View.GONE)
                views.setViewVisibility(R.id.divider, View.GONE)
            }

            Log.d("WidgetUpdater", "Updating widget $appWidgetId with label $label, original/target date $date")

            Log.d("WidgetUpdater", "The new difference is $difference. The current time is $currentTime")

            val color = prefs.getInt(appWidgetId.toString() + "color", Color.BLUE)

            views.setInt(R.id.bkgView, "setColorFilter", color)

            val hsv = FloatArray(3)

            Color.colorToHSV(color, hsv)

            val brightness = (1 - hsv[1] + hsv[2]) / 2

            if (brightness >= 0.7) {
                views.setTextColor(R.id.txtLabel, Color.BLACK)
                views.setTextColor(R.id.txtDays, Color.BLACK)
                views.setTextColor(R.id.txtThereAreHaveBeen, Color.BLACK)
                views.setTextColor(R.id.txtNoDays, Color.BLACK)
            }

            views.setInt(R.id.lytWidget, "setVisibility", View.VISIBLE)

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        var WIDGET_BUTTON = "com.agusyc.daycounter.RESET_COUNTER"
    }
}