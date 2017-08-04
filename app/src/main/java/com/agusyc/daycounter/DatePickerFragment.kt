package com.agusyc.daycounter

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.SimpleDateFormat
import java.util.*

class DatePickerFragment(val edtText: EditText) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // We make the picker be on the current date by default
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(activity, this, year, month, day)
    }

    // This method is executed when the user selects a date
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        Log.d("DatePickerFragment", "User picked year, month, day: " + year + " " + month + 1 + " " + day)
        if (activity is ConfigurationActivity) {
            // If the picker is being used from the configuration activity, this block is ran.
            val spnType = activity.findViewById<Spinner>(R.id.spnType)

            // We calculate the difference in days
            val currentTime = System.currentTimeMillis()
            // We use the Joda-Time method to calculate the difference
            val difference = Days.daysBetween(DateTime().withDate(year, month + 1, day), DateTime(currentTime).withTime(0, 0, 0, 0)).days

            // We set the spinner on the configuration activity depending on the sign of the difference (Until, since or today)
            if (difference >= 0) {
                spnType.setSelection(1)
            } else {
                spnType.setSelection(2)
            }

            // We set the days text to the *absolute* difference
            edtText.setText(Math.abs(difference).toString())
        } else if (activity is MainActivity) {
            // If the picker is being used from the MainActivity (The calculator dialog), this block is ran.
            // We get the selected date, parse it and then set the edtText to the formatted string
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val cal = Calendar.getInstance()
            cal.set(year, month, day, 0, 0, 0)
            edtText.setText(sdf.format(cal.time))
        }
    }
}
