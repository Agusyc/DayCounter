package com.agusyc.daycounter

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.support.v4.app.NotificationCompat
import org.joda.time.DateTime
import org.joda.time.Days
import android.app.PendingIntent




class CounterNotificator : BroadcastReceiver() {
    var res: Resources? = null
    var nm: NotificationManager? = null

    override fun onReceive(context: Context, intent: Intent) {
        res = context.resources
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (intent.action == ACTION_UPDATE_NOTIFICATIONS) {
            if (intent.hasExtra("widget_ids"))
                notify(intent.getIntArrayExtra("widget_ids"), true, context)
            if (intent.hasExtra("list_ids"))
                notify(intent.getIntArrayExtra("list_ids"), false, context)
        }
    }

    internal fun notify(ids: IntArray, areWidget: Boolean, context: Context) {
        for (id in ids) {
            val counter = Counter(context, id, areWidget)

            if (counter.notification) {
                val currentTime = System.currentTimeMillis()
                // We use the Joda-Time method to calculate the difference
                val difference = Days.daysBetween(DateTime(counter.date), DateTime(currentTime)).days
                val absDifference = Math.abs(difference)
                val contentText: String
                if (difference > 0) {
                    contentText = String.format("%s %d %s", res!!.getQuantityString(R.plurals.there_has_have_been, absDifference), absDifference, res!!.getQuantityString(R.plurals.days_since, absDifference, counter.label))
                } else if (difference < 0) {
                    contentText = String.format("%s %d %s", res!!.getQuantityString(R.plurals.there_is_are, absDifference), absDifference, res!!.getQuantityString(R.plurals.days_until, absDifference, counter.label))
                } else {
                    contentText = context.getString(R.string.there_are_no_days_since, counter.label)
                }

                val notificationIntent = Intent(context, MainActivity::class.java)

                notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

                val main_act_intent = PendingIntent.getActivity(context, 0,
                        notificationIntent, 0)

                val mBuilder = NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.reset_counter)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setOngoing(true)
                        .setColor(counter.color)
                        .setContentIntent(main_act_intent)
                        .setContentText(contentText)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setCategory(Notification.CATEGORY_STATUS)
                        .setPriority(Notification.PRIORITY_MIN)

                nm!!.notify(id, mBuilder.build())
            }
        }
    }

    companion object {
        internal val ACTION_UPDATE_NOTIFICATIONS = "com.agusyc.daycounter.ACTION_UPDATE_NOTIFICATIONS"
    }
}