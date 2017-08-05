package com.agusyc.daycounter

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CalculatorDialog(context: Context?, val themeResId: Int) : AppCompatDialog(context, themeResId), View.OnClickListener, DatePickerDialog.OnDateSetListener {
    // We declare all the vars here to use it on all methods
    private lateinit var edtStartDate: EditText
    private lateinit var edtEndDate: EditText
    private lateinit var txtResult: TextView
    private lateinit var datePickerEnd: DatePickerDialog
    private lateinit var datePickerStart: DatePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // We set the dialog title
        setTitle(context.getString(R.string.calculator_short))
        setContentView(R.layout.calculator_dialog)

        // We initialise all the views
        edtStartDate = findViewById(R.id.edtStartDate) as EditText
        edtEndDate = findViewById(R.id.edtEndDate) as EditText
        txtResult = findViewById(R.id.txtResult) as TextView
        val cal = Calendar.getInstance()
        datePickerEnd = DatePickerDialog(context, this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        datePickerStart = DatePickerDialog(context, this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        findViewById(R.id.btnCalculate)!!.setOnClickListener(this)
        val btnStartDate = findViewById(R.id.btnStartDate) as ImageView
        btnStartDate.setOnClickListener(this)
        val btnEndDate = findViewById(R.id.btnEndDate) as ImageView
        btnEndDate.setOnClickListener(this)
        // If the theme is dark, we paint the buttons
        if (themeResId == R.style.CalculatorDarkTheme) {
            btnStartDate.setColorFilter(Color.WHITE)
            btnEndDate.setColorFilter(Color.WHITE)
            edtStartDate.setTextColor(Color.WHITE)
            edtEndDate.setTextColor(Color.WHITE)
        }

        // We make the dialog be canceled when being touched outside of it
        setCanceledOnTouchOutside(true)
    }

    override fun onClick(view: View) {
        // We check which view was clicked
        if (view.id == R.id.btnCalculate) {
            try {
                if (edtStartDate.text.isEmpty()) {
                    // The start date text is empty
                    edtStartDate.error = context.getString(R.string.input_something_error)
                    return
                } else edtStartDate.error = null
                if (edtEndDate.text.isEmpty()) {
                    // The end date text is empty
                    edtEndDate.error = context.getString(R.string.input_something_error)
                    return
                } else edtEndDate.error = null
                // We parse the date so we can calculate de difference
                val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                df.isLenient = false
                val startDate = df.parse(edtStartDate.text.toString())
                val endDate = df.parse(edtEndDate.text.toString())
                // We use Joda-Time to calculate the days between the dates
                val days = Days.daysBetween(DateTime(startDate), DateTime(endDate)).days
                // We set the result text accordingly
                txtResult.text = if (days != 0) context.resources.getQuantityString(R.plurals.difference, days, days) else context.getString(R.string.no_difference)
            } catch (e: ParseException) {
                // One of the dates was badly formatted, so we show an error dialog
                val adb = AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog)
                adb.setTitle(R.string.bad_format_title)
                adb.setMessage(R.string.bad_format_message)
                adb.setNeutralButton(R.string.ok, null)
                adb.create().show()
            }
        } else if (view.id == R.id.btnEndDate) {
            // We show the datePicker and send it the end date editView
            datePickerEnd.show()
        } else if (view.id == R.id.btnStartDate) {
            // We show the datePicker and send it the start date editView
            datePickerStart.show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, day: Int, month: Int) {
        val id = view!!.id
        Log.d("Calculator", "Start is " + R.id.edtStartDate)
        Log.d("Calculator", "End is " + R.id.edtEndDate)
        Log.d("Calculator", "Chosen is " + id)
        // We get the selected date, parse it and then set the edtText to the formatted string
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(year, month, day, 0, 0, 0)
        if (id == R.id.edtStartDate) edtStartDate.setText(sdf.format(cal.time)) else edtEndDate.setText(sdf.format(cal.time))
    }
}
