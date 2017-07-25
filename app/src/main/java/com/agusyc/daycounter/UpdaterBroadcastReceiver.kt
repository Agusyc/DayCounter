package com.agusyc.daycounter

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

class UpdaterBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_DATE_CHANGED || intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val updaterIntent = Intent(context, WidgetUpdater::class.java)
            updaterIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

            Log.d("UpdaterReceiver", "Broadcast received! Updating widgets...")

            val prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)


            val IDs_set = prefs.getStringSet("ids", HashSet<String>())

            val IDs_array_str = IDs_set!!.toTypedArray<String>()

            val IDs_array = IntArray(IDs_array_str.size)

            for (i in IDs_array_str.indices) {
                IDs_array[i] = Integer.parseInt(IDs_array_str[i])
                Log.d("UpdateReceiver", "Parsed ID: " + IDs_array[i])
            }

            updaterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, IDs_array)

            Log.d("UpdateReceiver", "Telling the WidgetUpdater to start")
            context.sendBroadcast(updaterIntent)
        }
    }
}
