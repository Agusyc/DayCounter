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
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class DatePickerFragment(val edtText: EditText) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        Log.d("DatePickerFragment", "User picked year, month, day: " + year + " " + month + 1 + " " + day)
        if (activity is ConfigurationActivity) {
            val spnType = activity.findViewById<Spinner>(R.id.spnType)

            val currentTime = System.currentTimeMillis()

            // We use the Joda-Time method to calculate the difference
            val difference = Days.daysBetween(DateTime().withDate(year, month + 1, day), DateTime(currentTime)).days

            if (difference >= 0) {
                spnType.setSelection(1)
            } else {
                spnType.setSelection(2)
            }

            val formatter = DecimalFormat("#,###,###")

            // We set the days text to the *absolute* difference
            edtText.setText(formatter.format(Math.abs(difference)))
        } else if (activity is MainActivity) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val cal = Calendar.getInstance()
            cal.set(year, month, day, 0, 0, 0)
            edtText.setText(sdf.format(cal.time))
        }
    }
}