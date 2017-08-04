package com.agusyc.daycounter

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*


class DayCounter : Application() {

    // This gets executed when the app is started
    override fun onCreate() {
        super.onCreate()
        Log.i("DayCounter", "DayCounter was started")
	// We update all our notifications
        CounterNotificator().updateAll(applicationContext)
        
	// We set up an alarm that repeats itself at midnight everyday, and updates all the counters
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
