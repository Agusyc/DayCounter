package com.agusyc.daycounter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import org.joda.time.DateTime
import org.joda.time.Days
import java.util.*


class CounterNotificator : BroadcastReceiver() {
    // This variables represent the context resources and system notification manager
    var res: Resources? = null
    private var nm: NotificationManager? = null

    override fun onReceive(context: Context, intent: Intent) {
        // We initialise the variables
        res = context.resources
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // We check if the intent is right
        if (intent.action == ACTION_UPDATE_NOTIFICATIONS) {
            // We notify all the widget counter
            if (intent.hasExtra("widget_ids"))
                notify(intent.getIntArrayExtra("widget_ids"), true, context)
            // We notify all the list counters
            if (intent.hasExtra("list_ids"))
                notify(intent.getIntArrayExtra("list_ids"), false, context)
        }
    }

    private fun notify(ids: IntArray, areWidget: Boolean, context: Context) {
        // We go trough each id in the array
        for (id in ids) {
            // The current counter being processed by the for loop:
            val counter = Counter(context, id, areWidget)

            // We only notify it if it's a notification-enabled counter
            if (counter.notification) {
                // We get the current time, for calculating the difference
                val currentTime = System.currentTimeMillis()
                // We use the Joda-Time method to calculate the difference
                val difference = Days.daysBetween(DateTime(counter.date), DateTime(currentTime)).days
                val absDifference = Math.abs(difference)
                val contentText: String
                // We set the contentText depeding on the sign of the difference (Positive is since, negative is until and 0 is today)
                contentText = when {
                    difference > 0 -> String.format("%s %d %s", res!!.getQuantityString(R.plurals.there_has_have_been, absDifference), absDifference, res!!.getQuantityString(R.plurals.days_since, absDifference, counter.label))
                    difference < 0 -> String.format("%s %d %s", res!!.getQuantityString(R.plurals.there_is_are, absDifference), absDifference, res!!.getQuantityString(R.plurals.days_until, absDifference, counter.label))
                    else -> context.getString(R.string.there_are_no_days_since, counter.label)
                }

                // Variables for starting the MainActivity when the notification is clicked
                val notificationIntent = Intent(context, MainActivity::class.java)
                notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                val main_act_intent = PendingIntent.getActivity(context, 0,
                        notificationIntent, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // The id of the channel.
                    val channel_id = "daycounterchannel"
                    // The user-visible name of the channel.
                    val name = "DayCounter"
                    // The user-visible description of the channel.
                    val description = context.getString(R.string.channel_description)
                    val importance = NotificationManager.IMPORTANCE_MIN
                    val mChannel = NotificationChannel(channel_id, name, importance)
                    // Configure the notification channel.
                    mChannel.description = description
                    mChannel.enableLights(false)
                    mChannel.enableVibration(false)
                    mChannel.setShowBadge(false)
                    nm!!.createNotificationChannel(mChannel)


                    // We build and notify the notification
                    val mBuilder = Notification.Builder(context, channel_id)
                            .setSmallIcon(R.drawable.reset_counter)
                            .setContentTitle(context.getString(R.string.app_name))
                            .setOngoing(true)
                            .setColor(counter.color)
                            .setContentIntent(main_act_intent)
                            .setContentText(contentText)
                            .setCategory(Notification.CATEGORY_REMINDER)
                            .setShowWhen(false)
                            nm!!.notify(id, mBuilder.build())
                } else {
                    Log.i("DayCounter", "Using old notification system")
                    // We build and notify the notification
                    @Suppress("DEPRECATION")
                    val mBuilder = NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.reset_counter)
                            .setContentTitle(context.getString(R.string.app_name))
                            .setOngoing(true)
                            .setColor(counter.color)
                            .setContentIntent(main_act_intent)
                            .setContentText(contentText)
                            .setCategory(Notification.CATEGORY_STATUS)
                            .setPriority(Notification.PRIORITY_MIN)
                            .setShowWhen(false)
                            nm!!.notify(id, mBuilder.build())
                }
            }
        }
    }

    // This method takes care of executing the previous methods to update every counter.
    // It parses every ID in the set and sends it to the notificator
    fun updateAll(context: Context) {
        val updaterIntent = Intent(context, CounterNotificator::class.java)
        updaterIntent.action = CounterNotificator.ACTION_UPDATE_NOTIFICATIONS
        var prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)

        var IDs_set = prefs.getStringSet("ids", HashSet<String>())

        var IDs_array_str = IDs_set!!.toTypedArray<String>()

        var IDs_array = IntArray(IDs_array_str.size)

        for (i in IDs_array_str.indices) {
            IDs_array[i] = Integer.parseInt(IDs_array_str[i])
            Log.d("UpdateReceiver", "Parsed ID: " + IDs_array[i])
        }

        updaterIntent.putExtra("widget_ids", IDs_array)

        prefs = context.getSharedPreferences("ListDaysPrefs", Context.MODE_PRIVATE)

        IDs_set = prefs.getStringSet("ids", HashSet<String>())

        IDs_array_str = IDs_set!!.toTypedArray()

        IDs_array = IntArray(IDs_array_str.size)

        for (i in IDs_array_str.indices) {
            IDs_array[i] = Integer.parseInt(IDs_array_str[i])
            Log.d("UpdateReceiver", "Parsed ID: " + IDs_array[i])
        }

        updaterIntent.putExtra("list_ids", IDs_array)
        Log.d("UpdateReceiver", "Telling the CounterNotificator to start")
        context.sendBroadcast(updaterIntent)
    }

    companion object {
        internal val ACTION_UPDATE_NOTIFICATIONS = "com.agusyc.daycounter.ACTION_UPDATE_NOTIFICATIONS"
    }
}
