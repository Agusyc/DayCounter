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
import java.text.DecimalFormat
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
            // We get the widget's id which reset button was clicked
            val id_s = intent.action!!.replace(WIDGET_BUTTON, "")

            Log.d("WidgetUpdater", "The reset counter button was clicked for id " + id_s)

            val prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)

            // We set the date to today's
            prefs.edit().putLong(id_s + "date", DateTime.now().withTime(0, 0, 0, 0).millis).apply()

            // We tell the Notificator to update this counter
            val updaterIntent = Intent(context, CounterNotificator::class.java)
            updaterIntent.action = CounterNotificator.ACTION_UPDATE_NOTIFICATIONS
            updaterIntent.putExtra("widget_ids", intArrayOf(Integer.parseInt(id_s)))
            context.sendBroadcast(updaterIntent)

            // We update this widget
            onUpdate(context, AppWidgetManager.getInstance(context), intArrayOf(Integer.parseInt(id_s)))
        } else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == intent.action) {
            // We update all the widgets that are in the EXTRA_APPWIDGET_IDS array
            Log.d("WidgetUpdater", "Updating widgets...")
            val ids = intent.extras!!.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            onUpdate(context, AppWidgetManager.getInstance(context), ids)
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

        // We remove all the counter's data from the notifications
        val prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)
        prefs.edit().remove(id.toString() + "label").apply()
        prefs.edit().remove(id.toString() + "date").apply()
        prefs.edit().remove(id.toString() + "color").apply()
        prefs.edit().remove(id.toString() + "color_index").apply()
        prefs.edit().remove(id.toString() + "notification").apply()

        val ids_set = prefs.getStringSet("ids", HashSet<String>())

        // We iterate trough the whole set, when we find the widget that is being deleted, we take it out of the set without mercy!
        val iterator = ids_set!!.iterator()
        while (iterator.hasNext()) {
            if (iterator.next() == Integer.toString(id)) {
                iterator.remove()
                break
            }
        }

        // We put the new set to the prefs
        prefs.edit().putStringSet("ids", ids_set).apply()
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val res = context.resources

        // We go trough each widget that is being updated
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
            val difference = Days.daysBetween(DateTime(date), DateTime(currentTime)).days
            val formatter = DecimalFormat("#,###,###")
            val absDifference = Math.abs(difference)

            // We check the sign of the number (Positive or negative). So we know if we use "since" or "until"
            if (difference > 0) {
                // We set the right layout
                views = RemoteViews(context.packageName, R.layout.daycounter_since)
                // We update all the views
                views.setTextViewText(R.id.txtThereAreHaveBeen, res.getQuantityString(R.plurals.there_is_are, absDifference))
                views.setTextViewText(R.id.txtLabel, res.getQuantityString(R.plurals.days_since, absDifference, if (label.length >= 17) label.substring(0, 16) + "..." else label))
                views.setTextViewText(R.id.txtDays, formatter.format(absDifference))
                views.setOnClickPendingIntent(R.id.btnReset, getPendingSelfIntent(context, WIDGET_BUTTON, appWidgetId))

                // We check the brightness and auto-color the texts and icons accordingly
                if (brightness >= 0.65) {
                    views.setTextColor(R.id.txtThereAreHaveBeen, Color.BLACK)
                    views.setTextColor(R.id.txtLabel, Color.BLACK)
                    views.setTextColor(R.id.txtDays, Color.BLACK)
                    views.setInt(R.id.btnReset, "setColorFilter", Color.BLACK)
                }
            } else if (difference < 0) {
                // We set the right layout
                views = RemoteViews(context.packageName, R.layout.daycounter_until)
                // We update all the views
                views.setTextViewText(R.id.txtThereAreHaveBeen, res.getQuantityString(R.plurals.there_is_are, absDifference))
                views.setTextViewText(R.id.txtLabel, res.getQuantityString(R.plurals.days_until, absDifference, if (label.length >= 17) label.substring(0, 16) + "..." else label))
                views.setTextViewText(R.id.txtDays, formatter.format(absDifference))

                // We check the brightness and auto-color the texts and icons accordingly
                if (brightness >= 0.65) {
                    views.setTextColor(R.id.txtThereAreHaveBeen, Color.BLACK)
                    views.setTextColor(R.id.txtLabel, Color.BLACK)
                    views.setTextColor(R.id.txtDays, Color.BLACK)
                }
            } else {
                // We set the right layout and just set the noDays text
                views = RemoteViews(context.packageName, R.layout.daycounter_nodays)
                views.setTextViewText(R.id.txtNoDays, context.getString(R.string.there_are_no_days_since, if (label.length >= 17) label.substring(0, 16) + "..." else label))
            }

            // We set the background  color and we make the widget visible
            views.setInt(R.id.bkgView, "setColorFilter", color)
            views.setInt(R.id.lytWidget, "setVisibility", View.VISIBLE)

            // We actually tell the widget manager to perform all the previous updates
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        var WIDGET_BUTTON = "com.agusyc.daycounter.RESET_COUNTER"
    }
}
