package com.agusyc.daycounter

import android.app.NotificationManager
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

            val updaterIntent = Intent(context, CounterNotificator::class.java)
            updaterIntent.action = CounterNotificator.ACTION_UPDATE_NOTIFICATIONS
            updaterIntent.putExtra("widget_ids", intArrayOf(Integer.parseInt(id_s)))
            context.sendBroadcast(updaterIntent)

            onUpdate(context, AppWidgetManager.getInstance(context), intArrayOf(Integer.parseInt(id_s)))
        } else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == intent.action) {
            Log.d("WidgetUpdater", "Updating widgets...")
            val ids = intent.extras!!.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            // We update the widgets
            onUpdate(context, AppWidgetManager.getInstance(context), ids)
        } else if ("android.appwidget.action.APPWIDGET_UPDATE_OPTIONS" == intent.action) {
            onUpdate(context, AppWidgetManager.getInstance(context), intArrayOf(intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)))
        }
    }

    private fun getPendingSelfIntent(context: Context, action: String, id: Int): PendingIntent {
        val intent = Intent(context, javaClass)
        intent.action = action + id
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    private fun deleteWidget(context: Context, id: Int) {
        Log.d("MainActivity", "Removing widget with id " + id)

        // We dismiss the notification
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(id)

        // We remove the label, date and color from the preferences
        val prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)
        prefs.edit().remove(id.toString() + "label").apply()
        prefs.edit().remove(id.toString() + "date").apply()
        prefs.edit().remove(id.toString() + "color").apply()
        prefs.edit().remove(id.toString() + "color_index").apply()
        prefs.edit().remove(id.toString() + "notification").apply()

        val ids_set = prefs.getStringSet("ids", HashSet<String>())

        // We iterate trough the whole set, when we find the widget that is being deleted, we delete it from the set without mercy!
        val iterator = ids_set!!.iterator()
        while (iterator.hasNext()) {
            if (iterator.next() == Integer.toString(id)) {
                Log.d("WidgetUpdater", "Removing counter ID from preferences")
                iterator.remove()
                break
            }
        }

        // We put the new set to the prefs
        prefs.edit().putStringSet("ids", ids_set).apply()
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d("WidgetUpdater", "WidgetUpdater onUpdate started")

        val res = context.resources

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (appWidgetId in appWidgetIds) {
            val views: RemoteViews

            // / We get all the needed data
            val counter = Counter(context, appWidgetId, true)
            val label = counter.label
            val date = counter.date
            val currentTime = System.currentTimeMillis()
            val color = counter.color
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            val brightness = (1 - hsv[1] + hsv[2]) / 2

            // We use the Joda-Time method to calculate the difference
            val difference = Days.daysBetween(DateTime(date), DateTime(currentTime)).days
            val absDifference = Math.abs(difference)

            // We check the sign of the number (Positive or negative). So we know if we use "since" or "until"
            if (difference > 0) {
                views = RemoteViews(context.packageName, R.layout.daycounter_since)
                views.setTextViewText(R.id.txtThereAreHaveBeen, res.getQuantityString(R.plurals.there_is_are, absDifference))
                views.setTextViewText(R.id.txtLabel, res.getQuantityString(R.plurals.days_since, absDifference, if (label.length >= 17) label.substring(0, 16) + "..." else label))
                views.setTextViewText(R.id.txtDays, Integer.toString(absDifference))

                if (brightness >= 0.65) {
                    views.setTextColor(R.id.txtThereAreHaveBeen, Color.BLACK)
                    views.setTextColor(R.id.txtLabel, Color.BLACK)
                    views.setTextColor(R.id.txtDays, Color.BLACK)
                    views.setInt(R.id.btnReset, "setColorFilter", Color.BLACK)
                }

                views.setOnClickPendingIntent(R.id.btnReset, getPendingSelfIntent(context, WIDGET_BUTTON, appWidgetId))
            } else if (difference < 0) {
                views = RemoteViews(context.packageName, R.layout.daycounter_until)
                views.setTextViewText(R.id.txtThereAreHaveBeen, res.getQuantityString(R.plurals.there_is_are, absDifference))
                views.setTextViewText(R.id.txtLabel, res.getQuantityString(R.plurals.days_until, absDifference, if (label.length >= 17) label.substring(0, 16) + "..." else label))
                views.setTextViewText(R.id.txtDays, Integer.toString(absDifference))

                if (brightness >= 0.65) {
                    views.setTextColor(R.id.txtThereAreHaveBeen, Color.BLACK)
                    views.setTextColor(R.id.txtLabel, Color.BLACK)
                    views.setTextColor(R.id.txtDays, Color.BLACK)
                }
            } else {
                views = RemoteViews(context.packageName, R.layout.daycounter_nodays)
                views.setTextViewText(R.id.txtNoDays, context.getString(R.string.there_are_no_days_since, if (label.length >= 17) label.substring(0, 16) + "..." else label))
            }

            Log.d("WidgetUpdater", "Updating widget $appWidgetId with label $label, original/target date $date")

            Log.d("WidgetUpdater", "The new difference is $difference. The current time is $currentTime")

            views.setInt(R.id.bkgView, "setColorFilter", color)
            views.setInt(R.id.lytWidget, "setVisibility", View.VISIBLE)

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        var WIDGET_BUTTON = "com.agusyc.daycounter.RESET_COUNTER"
    }
}
