package com.agusyc.daycounter

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.util.Log
import android.app.PendingIntent
import android.content.Intent
import java.util.*


class DayCounter : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("DayCounter", "DayCounter was started")
        CounterNotificator().updateAll(applicationContext)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val intent = Intent(applicationContext, UpdaterBroadcastReceiver::class.java)
        intent.action = UpdaterBroadcastReceiver.ACTION_UPDATE_ALL
        val pi = PendingIntent.getBroadcast(applicationContext, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val am = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY, pi)
    }
}