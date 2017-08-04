package com.agusyc.daycounter

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

class UpdaterBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)

        // We parse all the widget data
        val IDs_set = prefs.getStringSet("ids", HashSet<String>())
        val IDs_array_str = IDs_set!!.toTypedArray<String>()
        val IDs_array = IntArray(IDs_array_str.size)

        for (i in IDs_array_str.indices) {
            IDs_array[i] = Integer.parseInt(IDs_array_str[i])
            Log.d("UpdateReceiver", "Parsed ID: " + IDs_array[i])
        }

        // We create and set the Intent up, then we send it to the WidgetUpdater
        val updaterIntent = Intent(context, WidgetUpdater::class.java)
        updaterIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        updaterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, IDs_array)
        context.sendBroadcast(updaterIntent)

        // We tell the Notificator to update all the counters
        CounterNotificator().updateAll(context)
    }

    companion object {
        val ACTION_UPDATE_ALL = "com.agusyc.daycounter.UPDATE_ALL"
    }
}
