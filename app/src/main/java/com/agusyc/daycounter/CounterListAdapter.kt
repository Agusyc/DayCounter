package com.agusyc.daycounter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ArrayAdapter
import android.widget.TextView
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.DecimalFormat
import java.util.*

internal class CounterListAdapter(context: Context, alarms: ArrayList<Counter>) : ArrayAdapter<Counter>(context, 0, alarms) {
    // This represents the context resources, for getting quantityStrings.
    val res = context.resources!!

    // This method runs as much times as there are counters defined by the user, so it adds everyone to the listview.
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // We get the counter for the current position
        val counter = getItem(position)

        val newConvertView: View

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            newConvertView = LayoutInflater.from(context).inflate(R.layout.counter_row, parent, false)
        } else {
            newConvertView = convertView
        }

        // We get all the needed data, views and objects
        val date = counter!!.date
        val currentTime = System.currentTimeMillis()
        val difference = Days.daysBetween(DateTime(date), DateTime(currentTime)).days
        val absDifference = Math.abs(difference)
        val formatter = DecimalFormat("#,###,###")

        // We initialise all the views
        val txtDays = newConvertView.findViewById<TextView>(R.id.txtDays)
        // We set the days text to the *absolute* difference	
        txtDays.text = formatter.format(absDifference)
        val txtLabel = newConvertView.findViewById<TextView>(R.id.txtLabel)
        val txtThereAreHaveBeen = newConvertView.findViewById<TextView>(R.id.txtThereAreHaveBeen)
        val btnReset = newConvertView.findViewById<ResetButton>(R.id.btnReset)

        // We check the sign of the number (Positive or negative, since or until), so we can set up the list's views accordingly
        if (difference > 0) {
            // The counter's date is behind current time
            txtLabel.text = context.resources.getQuantityString(R.plurals.days_since, absDifference, counter.label)
            btnReset.widget_id = counter.id
            btnReset.isWidget = counter.isWidget
            txtDays.visibility = View.VISIBLE
            btnReset.visibility = View.VISIBLE
            txtThereAreHaveBeen.visibility = View.VISIBLE
            txtThereAreHaveBeen.text = res.getQuantityText(R.plurals.there_has_have_been, absDifference)
        } else if (difference < 0) {
            // The counter's date is after the current time
            txtLabel.text = res.getQuantityString(R.plurals.days_until, absDifference, counter.label)
            txtDays.visibility = View.VISIBLE
            txtThereAreHaveBeen.visibility = View.VISIBLE
            txtThereAreHaveBeen.text = res.getQuantityText(R.plurals.there_is_are, absDifference)
            btnReset.visibility = View.GONE
        } else {
            // The counter's date is today
            txtLabel.text = context.getString(R.string.there_are_no_days_since, counter.label)
            txtLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)

            txtDays.visibility = View.GONE
            btnReset.visibility = View.GONE
            txtThereAreHaveBeen.visibility = View.GONE
        }

        Log.i("CounterListAdapter", "Adding widget " + counter.id + " with label " + counter.label + ", original/target date " + date)

        Log.i("CounterListAdapter", "The new difference is $difference. The current time is $currentTime")

        // We get the counter's color
        val color = counter.color

        // We set the item's background color
        newConvertView.setBackgroundColor(color)

        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        // We calculate the brightness of the background color and set icons and text color accordingly
        val brightness = (1 - hsv[1] + hsv[2]) / 2
        if (brightness >= 0.65) {
            txtDays.setTextColor(Color.BLACK)
            txtLabel.setTextColor(Color.BLACK)
            (newConvertView.findViewById<TextView>(R.id.txtThereAreHaveBeen) as TextView).setTextColor(Color.BLACK)
            btnReset.setColorFilter(Color.BLACK)
        }

        // This is for controlling the animations when adding a new widget
        if (!(context as MainActivity).dontAnimate) {
            val animation = AlphaAnimation(0f, 1.0f)
            animation.duration = 400
            animation.startOffset = 100
            animation.fillAfter = true
            newConvertView.startAnimation(animation)
        }

        // Return the completed view to render on the main activity
        return newConvertView
    }
}
