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
import java.util.Calendar

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceSateate: Bundle): Dialog {

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        Log.d("DatePickerFragment", "User picked year, month, day: " + year + " " + month + 1 + " " + day)

        val edtDays = activity.findViewById<EditText>(R.id.edtDays)
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
        edtDays.setText(formatter.format(Math.abs(difference).toLong()))
    }
}