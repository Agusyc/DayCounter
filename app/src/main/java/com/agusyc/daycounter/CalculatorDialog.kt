package com.agusyc.daycounter

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.view.View
import android.widget.EditText
import android.widget.TextView
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CalculatorDialog(context: Context) : AppCompatDialog(context), View.OnClickListener {
    private var edtStartDate: EditText? = null
    private var edtEndDate: EditText? = null
    private var txtResult: TextView? = null

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calculator_dialog)
        edtStartDate = findViewById(R.id.edtStartDate) as EditText?
        edtEndDate = findViewById(R.id.edtEndDate) as EditText?
        txtResult = findViewById(R.id.txtResult) as TextView?
        val btnCalculate = findViewById(R.id.btnCalculate)!!
        btnCalculate.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btnCalculate) {
            try {
                val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                df.isLenient = false
                val startDate = df.parse(edtStartDate!!.text.toString())
                val endDate = df.parse(edtEndDate!!.text.toString())
                txtResult!!.text = context.getString(R.string.difference, Days.daysBetween(DateTime(startDate), DateTime(endDate)).days)
            } catch (e: ParseException) {
                val adb = AlertDialog.Builder(context)
                adb.setTitle(R.string.bad_format_title)
                adb.setMessage(R.string.bad_format_message)
                adb.setNeutralButton(R.string.ok, null)
                adb.create().show()
            }

        }
    }
}
