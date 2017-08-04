package com.agusyc.daycounter

import android.app.Dialog
import android.app.FragmentManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.TextView
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CalculatorDialog(context: Context?, themeResId: Int, val fragmentManager: FragmentManager) : Dialog(context, themeResId), View.OnClickListener {
    // We declare all the vars here to use it on all methods
    private var edtStartDate: EditText? = null
    private var edtEndDate: EditText? = null
    private var txtResult: TextView? = null
    private var datePickerEnd: DatePickerFragment? = null
    private var datePickerStart: DatePickerFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // We set the dialog title
        setTitle(context.getString(R.string.calculator_short))
        setContentView(R.layout.calculator_dialog)

        // We initialise all the views
        edtStartDate = findViewById<EditText>(R.id.edtStartDate)
        edtEndDate = findViewById<EditText>(R.id.edtEndDate)
        txtResult = findViewById<TextView>(R.id.txtResult)
        datePickerEnd = DatePickerFragment(edtEndDate as EditText)
        datePickerStart = DatePickerFragment(edtStartDate as EditText)
        findViewById<View>(R.id.btnCalculate)!!.setOnClickListener(this)
        findViewById<View>(R.id.btnStartDate)!!.setOnClickListener(this)
        findViewById<View>(R.id.btnEndDate)!!.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        // We check which view was clicked
        if (view.id == R.id.btnCalculate) {
            try {
                if (edtStartDate!!.text.isEmpty()) {
                    // The start date text is empty
                    edtStartDate!!.error = context.getString(R.string.input_something_error)
                    return
                } else edtStartDate!!.error = null
                if (edtEndDate!!.text.isEmpty()) {
                    // The end date text is empty
                    edtEndDate!!.error = context.getString(R.string.input_something_error)
                    return
                } else edtEndDate!!.error = null
                // We parse the date so we can calculate de difference
                val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                df.isLenient = false
                val startDate = df.parse(edtStartDate!!.text.toString())
                val endDate = df.parse(edtEndDate!!.text.toString())
                // We use Joda-Time to calculate the days between the dates
                val days = Days.daysBetween(DateTime(startDate), DateTime(endDate)).days
                // We set the result text accordingly
                txtResult!!.text = if (days != 0) context.resources.getQuantityString(R.plurals.difference, days, days) else context.getString(R.string.no_difference)
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
            datePickerEnd!!.show(fragmentManager, "datePicker")
        } else if (view.id == R.id.btnStartDate) {
            // We show the datePicker and send it the start date editView
            datePickerStart!!.show(fragmentManager, "datePicker")
        }
    }
}
