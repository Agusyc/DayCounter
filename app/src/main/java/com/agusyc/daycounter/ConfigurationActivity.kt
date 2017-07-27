package com.agusyc.daycounter

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.pes.androidmaterialcolorpickerdialog.ColorPicker
import fr.ganfra.materialspinner.MaterialSpinner
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.DecimalFormat
import java.util.*

class ConfigurationActivity : AppCompatActivity() {

    private var mAppWidgetId: Int = 0
    private var selectedColor: Int = 0
    private var selectedColor_index: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)

        val colorView = findViewById(R.id.colorView) as GridLayout

        // We instantiate all the Views
        val edtDays = findViewById(R.id.edtDays) as EditText

        val edtLabel = findViewById(R.id.edtLabel) as EditText

        val spnType = findViewById(R.id.spnType) as MaterialSpinner

        val types = ArrayList<String>()
        types.add(getString(R.string.days_since, ""))
        types.add(getString(R.string.days_until, ""))

        // Creating adapter for the Spinner
        val dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spnType.adapter = dataAdapter

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        val prefs = getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)

        val widget: Widget

        if (intent.hasExtra("widget_id")) {
            widget = Widget(applicationContext, Integer.parseInt(intent.getStringExtra("widget_id")))

            selectedColor = widget.color
            selectedColor_index = widget.colorIndex

            val date = widget.date
            val currentTime = System.currentTimeMillis()

            val difference = Days.daysBetween(DateTime(date), DateTime(currentTime)).days

            val formatter = DecimalFormat("#,###,###")

            edtDays.setText(formatter.format(Math.abs(difference).toLong()))
            edtLabel.setText(widget.label)

            if (difference >= 0) {
                spnType.setSelection(1)
            } else {
                spnType.setSelection(2)
            }

            val hsv = FloatArray(3)
            Color.colorToHSV(selectedColor, hsv)
            hsv[2] *= 0.6f // We make the color darker with this

            val selected_view = colorView.getChildAt(selectedColor_index) as ImageView
            val checked = ContextCompat.getDrawable(applicationContext, R.drawable.checked)
            val circle = ContextCompat.getDrawable(applicationContext, R.drawable.circle)
            circle.colorFilter = PorterDuffColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP)

            selected_view.setImageDrawable(ColorImageView.getOverlay(circle, checked))
        } else {
            val colorImageView = colorView.getChildAt(4) as ColorImageView

            selectedColor = colorImageView.color
            selectedColor_index = 4

            val hsv = FloatArray(3)
            Color.colorToHSV(selectedColor, hsv)
            hsv[2] *= 0.6f // We make the color darker with this

            val checked = ContextCompat.getDrawable(applicationContext, R.drawable.checked)
            val circle = ContextCompat.getDrawable(applicationContext, R.drawable.circle)
            circle.colorFilter = PorterDuffColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP)

            colorImageView.setImageDrawable(ColorImageView.getOverlay(circle, checked))
        }

        val okButton = findViewById(R.id.okButton) as Button
        okButton.setOnClickListener(View.OnClickListener {
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)

            val key_base: String

            val currentIDs_set = prefs.getStringSet("ids", HashSet<String>())

            if (intent.hasExtra("widget_id")) {
                key_base = intent.getStringExtra("widget_id")
            } else {
                key_base = Integer.toString(mAppWidgetId)

                currentIDs_set!!.add(key_base)
            }


            try {
                var date = DateTime(System.currentTimeMillis())

                val days_s = edtDays.text.toString()

                if (days_s.isEmpty()) {
                    edtDays.error = getString(R.string.input_something_error)
                    return@OnClickListener
                }

                val days = Integer.parseInt(days_s)

                val selectedPosition = spnType.selectedItemPosition

                when (selectedPosition) {
                    0 -> {
                        spnType.error = getString(R.string.kind_of_event_error)
                        return@OnClickListener
                    }
                    1 -> date = date.minusDays(days)
                    2 -> date = date.plusDays(days + 1)
                }

                val label = edtLabel.text.toString()

                if (label.isEmpty()) {
                    edtLabel.error = getString(R.string.input_something_error)
                    return@OnClickListener
                }

                prefs.edit().putString(key_base + "label", edtLabel.text.toString()).apply()
                prefs.edit().putLong(key_base + "date", date.millis).apply()
                prefs.edit().putInt(key_base + "color", selectedColor).apply()
                prefs.edit().putInt(key_base + "color_index", selectedColor_index).apply()
                prefs.edit().putStringSet("ids", currentIDs_set).apply()

                Log.d("ConfigurationActivity", "Added new Widget with label" + edtLabel.text + ", ID " + key_base + " and date " + date.millis)

                val IDs_array_str = currentIDs_set!!.toTypedArray<String>()

                val IDs_array = IntArray(IDs_array_str.size)

                for (i in IDs_array_str.indices) {
                    IDs_array[i] = Integer.parseInt(IDs_array_str[i])
                    Log.d("UpdateReceiver", "Parsed ID: " + IDs_array[i])
                }

                val updaterIntent = Intent(applicationContext, WidgetUpdater::class.java)
                updaterIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

                updaterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, IDs_array)

                Log.d("UpdateReceiver", "Telling the WidgetUpdater to start")
                applicationContext.sendBroadcast(updaterIntent)

                setResult(Activity.RESULT_OK, resultValue)
                finish()
            } catch (e: NumberFormatException) {
                edtDays.error = getString(R.string.number_too_big_error)
            }
        })

        for (i in 0..colorView.childCount - 1 - 1) {
            val v = colorView.getChildAt(i) as ColorImageView
            val finalI = i
            v.setOnClickListener { view ->
                var in_view: ColorImageView
                for (j in 0..colorView.childCount - 1 - 1) {
                    in_view = colorView.getChildAt(j) as ColorImageView
                    val circle = ContextCompat.getDrawable(applicationContext, R.drawable.circle)
                    circle.colorFilter = PorterDuffColorFilter(in_view.color, PorterDuff.Mode.SRC_ATOP)

                    in_view.setImageDrawable(circle)
                }
                selectedColor = v.color
                selectedColor_index = finalI

                (colorView.getChildAt(colorView.childCount - 1) as ImageView).setImageDrawable(getDrawable(R.drawable.custom_color))

                val hsv = FloatArray(3)
                Color.colorToHSV(selectedColor, hsv)

                hsv[2] *= 0.6f // We make the color darker with this

                val brightness = (1 - hsv[1] + hsv[2]) / 2

                val clicked_view = view as ColorImageView
                val checked = ContextCompat.getDrawable(applicationContext, R.drawable.checked)
                val circle = ContextCompat.getDrawable(applicationContext, R.drawable.circle)

                if (brightness > 0.65) {
                    checked.setTint(Color.BLACK)
                } else {
                    checked.setTint(Color.WHITE)
                }

                circle.colorFilter = PorterDuffColorFilter(Color.HSVToColor(hsv), PorterDuff.Mode.SRC_ATOP)

                clicked_view.setImageDrawable(ColorImageView.getOverlay(circle, checked))
            }
        }

        val btnDate = findViewById(R.id.btnDate) as ImageView
        btnDate.setOnClickListener {
            val newFragment = DatePickerFragment()
            newFragment.show(fragmentManager, "datePicker")
        }

        val btnCustomColor = findViewById(R.id.btnCustomColor) as ImageView

        btnCustomColor.setOnClickListener {
            val cp = ColorPicker(this@ConfigurationActivity, 255, 255, 255)
            /* Show color picker dialog */
            cp.show()

            /* Set a new Listener called when user click "select" */
            cp.setCallback { color ->
                var in_view: ColorImageView
                for (j in 0..colorView.childCount - 1 - 1) {
                    in_view = colorView.getChildAt(j) as ColorImageView
                    val circle = ContextCompat.getDrawable(applicationContext, R.drawable.circle)
                    circle.colorFilter = PorterDuffColorFilter(in_view.color, PorterDuff.Mode.SRC_ATOP)

                    in_view.setImageDrawable(circle)
                }

                selectedColor = color
                selectedColor_index = colorView.childCount - 1

                val hsv = FloatArray(3)
                Color.colorToHSV(selectedColor, hsv)

                hsv[2] *= 0.6f // We make the color darker with this

                val brightness = (1 - hsv[1] + hsv[2]) / 2

                val checked = ContextCompat.getDrawable(applicationContext, R.drawable.checked)

                if (brightness > 0.65) {
                    checked.setTint(Color.BLACK)
                } else {
                    checked.setTint(Color.WHITE)
                }

                val circle = ContextCompat.getDrawable(applicationContext, R.drawable.circle)
                circle.colorFilter = PorterDuffColorFilter(Color.HSVToColor(hsv), PorterDuff.Mode.SRC_ATOP)

                btnCustomColor.setImageDrawable(ColorImageView.getOverlay(circle, checked))

                cp.dismiss()
            }
        }
    }
}
