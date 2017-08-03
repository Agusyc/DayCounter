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
    val res = context.resources!!

    // This method runs as much times as there are alarms defined by the user, so it adds everyone to the listview.
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // We get the widget for the current position
        val counter = getItem(position)

        val newConvertView: View

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            newConvertView = LayoutInflater.from(context).inflate(R.layout.counter_row, parent, false)
        } else {
            newConvertView = convertView
        }

        assert(counter != null)
        val date = counter!!.date
        val currentTime = System.currentTimeMillis()

        val difference = Days.daysBetween(DateTime(date), DateTime(currentTime)).days
        val absDifference = Math.abs(difference)

        val txtDays = newConvertView.findViewById<TextView>(R.id.txtDays)

        val formatter = DecimalFormat("#,###,###")

        // We set the days text to the *absolute* difference
        txtDays.text = formatter.format(absDifference)
        val txtLabel = newConvertView.findViewById<TextView>(R.id.txtLabel)
        val txtThereAreHaveBeen = newConvertView.findViewById<TextView>(R.id.txtThereAreHaveBeen)
        val btnReset = newConvertView.findViewById<ResetButton>(R.id.btnReset)

        // We check the sign of the number (Positive or negative)
        if (difference > 0) {
            txtLabel.text = context.resources.getQuantityString(R.plurals.days_since, absDifference, counter.label)
            btnReset.setWidgetID(counter.id)
            btnReset.setIsWidget(counter.isWidget)
            txtDays.visibility = View.VISIBLE
            btnReset.visibility = View.VISIBLE
            txtThereAreHaveBeen.visibility = View.VISIBLE
            txtThereAreHaveBeen.text = res.getQuantityText(R.plurals.there_has_have_been, absDifference)
        } else if (difference < 0) {
            txtLabel.text = res.getQuantityString(R.plurals.days_until, absDifference, counter.label)
            txtDays.visibility = View.VISIBLE
            txtThereAreHaveBeen.visibility = View.VISIBLE
            txtThereAreHaveBeen.text = res.getQuantityText(R.plurals.there_is_are, absDifference)
            btnReset.visibility = View.GONE
        } else {
            txtLabel.text = context.getString(R.string.there_are_no_days_since, counter.label)
            txtLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)

            txtDays.visibility = View.GONE
            btnReset.visibility = View.GONE
            txtThereAreHaveBeen.visibility = View.GONE
        }

        Log.d("WidgetUpdater", "Adding widget " + counter.id + " with label " + counter.label + ", original/target date " + date)

        Log.d("WidgetUpdater", "The new difference is $difference. The current time is $currentTime")

        val color = counter.color

        newConvertView.setBackgroundColor(color)

        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        val brightness = (1 - hsv[1] + hsv[2]) / 2

        if (brightness >= 0.65) {
            txtDays.setTextColor(Color.BLACK)
            txtLabel.setTextColor(Color.BLACK)
            (newConvertView.findViewById<TextView>(R.id.txtThereAreHaveBeen) as TextView).setTextColor(Color.BLACK)
            btnReset.setColorFilter(Color.BLACK)
        }

        Log.d("ListUpdater", "Don't animate is " + (context as MainActivity).dontAnimate)

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