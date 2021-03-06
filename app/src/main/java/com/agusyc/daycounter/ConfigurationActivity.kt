package com.agusyc.daycounter

import android.app.Activity
import android.app.DatePickerDialog
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.pes.androidmaterialcolorpickerdialog.ColorPicker
import org.joda.time.DateTime
import org.joda.time.Days
import java.util.*


class ConfigurationActivity : AppCompatActivity(), View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private var mAppWidgetId: Int = 0
    private var selectedColor: Int = 0
    private var selectedColor_index: Int = 0
    private var isWidget: Boolean = false
    private lateinit var colorView: GridLayout
    private lateinit var edtDays: EditText
    private lateinit var edtLabel: EditText
    private lateinit var spnType: MaterialSpinner
    private lateinit var swtNotification: Switch
    private lateinit var dateDialog: DatePickerDialog
    private lateinit var btnCustomColor: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // We get the selected theme and set it
        val settings = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val dark_theme = settings!!.getBoolean("dark_theme", false)
        if (dark_theme) setTheme(R.style.AppDarkTheme)
        setContentView(R.layout.activity_configuration)

        // We set the title that is shown in the ActionBar
        title = getString(R.string.configuration)

        // This if checks whether the device is a tablet or not. If the device is *not* a tablet, we force the portrait orientation
        if (applicationContext.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK < Configuration.SCREENLAYOUT_SIZE_LARGE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // We initialise all the views
        btnCustomColor = findViewById(R.id.btnCustomColor) as ImageView
        btnCustomColor.setOnClickListener(this)
        colorView = findViewById(R.id.colorView) as GridLayout
        edtDays = findViewById(R.id.edtDays) as EditText
        val cal = Calendar.getInstance()
        dateDialog = if (dark_theme) DatePickerDialog(this@ConfigurationActivity, R.style.DatePickerDarkTheme, this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)) else DatePickerDialog(this@ConfigurationActivity, this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        edtLabel = findViewById(R.id.edtLabel) as EditText
        spnType = findViewById(R.id.spnType) as MaterialSpinner
        swtNotification = findViewById(R.id.swtNotification) as Switch
        findViewById(R.id.okButton).setOnClickListener(this)
        val btnDate = findViewById(R.id.btnDate) as ImageView
        btnDate.setOnClickListener(this)
        if (dark_theme) {
            btnDate.setColorFilter(Color.WHITE)
            edtDays.setTextColor(Color.WHITE)
            edtLabel.setTextColor(Color.WHITE)
        }

        // We add strings to the Spinner
        val types = ArrayList<String>()
        types.add(getString(R.string.days_since_al))
        types.add(getString(R.string.days_until_al))
        // We create an adapter for it and set it
        val dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnType.adapter = dataAdapter

        val extras = intent.extras
        // This variable is only set when the counter is a widget
        mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        // We set the isWidget variable, that is an extra in the intent
        isWidget = intent.getBooleanExtra("isWidget", true)
        val counter: Counter

        // If it does have the extra, then the configuration activity was opened from the MainActivity, for editing an existing widget
        if (intent.hasExtra("counter_id")) {
            Log.d("DayCounter", "Editting existing widget")
            // The counter class takes care of parsing everything
            counter = Counter(applicationContext, Integer.parseInt(intent.getStringExtra("counter_id")), isWidget)

            // We set all the states and variables by reading them from the counter object
            selectedColor = counter.color
            selectedColor_index = counter.colorIndex
            val date = counter.date
            var currentTime = DateTime.now()
            currentTime = currentTime.withTime(0, 0, 0, 0)
            var difference = Days.daysBetween(DateTime(date), DateTime(currentTime)).days
            // This if sentence correct the 1-day difference off-set bug. I don't know why it happens, though...
            if (difference < 0) {
                difference++
            }
            edtDays.setText(Math.abs(difference).toString())
            edtLabel.setText(counter.label)
            if (difference >= 0) {
                spnType.setSelection(1)
            } else {
                spnType.setSelection(2)
            }
            // The whole hsv thing is for making the color darker
            val hsv = FloatArray(3)
            Color.colorToHSV(selectedColor, hsv)
            hsv[2] *= 0.6f
            val selected_view = colorView.getChildAt(selectedColor_index) as ImageView
            val checked = ContextCompat.getDrawable(applicationContext, R.drawable.checked)
            val circle = ContextCompat.getDrawable(applicationContext, R.drawable.circle)
            circle.colorFilter = PorterDuffColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP)
            selected_view.setImageDrawable(ColorImageView.getOverlay(circle, checked))
            swtNotification.isChecked = counter.notification
        } else {
            Log.d("DayCounter", "Creating new widget")
            // The widget is new, not an existing one. This block basically sets the default selected color
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

        // We set up all the color circles
        // We subtract 2 because we need a 0-index and to skip the last view (The CustomColor one)
        for (i in 0..colorView.childCount - 2) {
            // This is the current color view being processed by the for loop
            val current_colorView = colorView.getChildAt(i) as ColorImageView
            current_colorView.setOnClickListener { view ->
                // This is the block that gets executed when a circle is clicked
                var in_view: ColorImageView
                // See the previous comment on the substraction
                // This for loop restores all the circles to their default state
                for (j in 0..colorView.childCount - 2) {
                    in_view = colorView.getChildAt(j) as ColorImageView
                    val circle = ContextCompat.getDrawable(applicationContext, R.drawable.circle)
                    circle.colorFilter = PorterDuffColorFilter(in_view.color, PorterDuff.Mode.SRC_ATOP)

                    in_view.setImageDrawable(circle)
                }
                // we set these variables so the SharedPreferences are updated with them
                selectedColor = current_colorView.color
                selectedColor_index = i

                // We set the CustomColor circle's image to custom_color (That is, its default state)
                (colorView.getChildAt(colorView.childCount - 1) as ImageView).setImageDrawable(getDrawable(R.drawable.custom_color))

                // This hsv thing is for making the selected color darker
                val hsv = FloatArray(3)
                Color.colorToHSV(selectedColor, hsv)

                hsv[2] *= 0.6f

                // We initialise all the variables (Views and drawables, here)
                val clicked_view = view as ColorImageView
                val checked = ContextCompat.getDrawable(applicationContext, R.drawable.checked)
                val circle = ContextCompat.getDrawable(applicationContext, R.drawable.circle)

                // We calculate the brightness of the color and adjust the checked mark's color accordingly
                val brightness = (1 - hsv[1] + hsv[2]) / 2
                if (brightness > 0.65) {
                    checked.setTint(Color.BLACK)
                } else {
                    checked.setTint(Color.WHITE)
                }

                // We set the color filter of the circle to the darkened selected color
                circle.colorFilter = PorterDuffColorFilter(Color.HSVToColor(hsv), PorterDuff.Mode.SRC_ATOP)

                // We make an overlay of the auto-colored checked icon and the circle
                clicked_view.setImageDrawable(ColorImageView.getOverlay(circle, checked))
            }
        }
    }

    // This method inflates the option menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // We inflate it only if the counter being added is an existing widget, because only those can be deleted
        return if (intent.extras.containsKey("counter_id") && !intent.extras.getBoolean("isWidget")) {
            // Inflate the menu; this adds items to the action bar if it is present.
            menuInflater.inflate(R.menu.configuration_activity, menu)
            true
        } else {
            false
        }
    }

    // Method that handles when a button in the menu is clicked
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val menuId = item.itemId

        if (menuId == R.id.action_delete) {
            // We remove the label, date and color from the preferences
            // We know that the right file is ListDaysPrefs because the button only shows up when you select a list counter, not a widget one
            val prefs = applicationContext.getSharedPreferences("ListDaysPrefs", Context.MODE_PRIVATE)
            val id = intent.getStringExtra("counter_id")

            // We dismiss the notification
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(Integer.parseInt(id))

            // We remove all the SharedPreferences content
            prefs.edit().remove(id + "label").apply()
            prefs.edit().remove(id + "date").apply()
            prefs.edit().remove(id + "color").apply()
            prefs.edit().remove(id + "color_index").apply()
            prefs.edit().remove(id + "notification").apply()

            val ids_set = prefs.getStringSet("ids", HashSet<String>())

            // We iterate trough the whole set, when we find the widget that we want to delete, we take it out of the set without mercy!
            val iterator = ids_set!!.iterator()
            while (iterator.hasNext()) {
                if (iterator.next() == id) {
                    Log.d("WidgetUpdater", "Removing counter ID from preferences")
                    iterator.remove()
                    break
                }
            }

            // We put the new set to the prefs
            prefs.edit().putStringSet("ids", ids_set).apply()
            // We finish the activity, so the user doesn't edit a non-existent counter
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    // We finish the activity when the user leaves because they may delete or change the widget, and this activity won't notice
    // This way, we force the user to reload the configuration
    override fun onPause() {
        super.onPause()
        finish()
    }

    // This method handles all the clicks on views
    override fun onClick(view: View?) {
        // We get the clicked it
        val id = view!!.id

        // We check which view was clicked
        when (id) {
            R.id.okButton -> {
                // This whole blocks puts all the changes in the prefs
                val prefs: SharedPreferences = if (isWidget) getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE) else getSharedPreferences("ListDaysPrefs", Context.MODE_PRIVATE)
                // We need to get the right file depending on wether the counter is a widget or a list counter

                // This stores the id of the counter
                val key_base: String

                // We need the current IDs so we can add the new one to it
                val currentIDs_set = prefs.getStringSet("ids", HashSet<String>())

                // We check whether the counter is new or already existing
                key_base = if (intent.hasExtra("counter_id")) {
                    intent.getStringExtra("counter_id")
                } else {
                    if (isWidget)
                        Integer.toString(mAppWidgetId)
                    else {
                        // If the set is empty, we can't use the "last" method, so we just set the key base to the minimum integer
                        if (currentIDs_set.isEmpty()) {
                            currentIDs_set.add("0")
                            Log.d("ConfigurationACtivity", "Was empty")
                            "0"
                        } else {
                            (Integer.parseInt(currentIDs_set.last()) + 1).toString()
                        }
                    }
                }

                try {
                    // This var represents the current Date and Time
                    var date = DateTime.now()
                    // We remove all the time values
                    date = date.withTime(0, 0, 0, 0)

                    // This variable contains the selectedItem (Whether the counter is "since" or "until")
                    val selectedPosition = spnType.selectedItemPosition
                    if (selectedPosition == 0) {
                        spnType.errorColor = Color.parseColor("#E7492E")
                        spnType.error = getString(R.string.kind_of_event_error)
                        return
                    }

                    // The string that contains what the user wrote
                    val days_s = edtDays.text.toString()

                    // We check if it's empty or not, if it is, we show an error and stop this method
                    if (days_s.isEmpty()) {
                        edtDays.error = getString(R.string.input_something_error)
                        return
                    }

                    // We can parse the integer now that we checked if it's empty
                    val days = Integer.parseInt(days_s)

                    // We check what the position is and act accordingly. If it's the default one, we show an error and stop the method
                    when (selectedPosition) {
                        1 -> date = date.minusDays(days)
                        2 -> date = date.plusDays(days + 1)
                    }

                    // We get the label
                    val label = edtLabel.text.toString()

                    // Same as "days" checking
                    if (label.isEmpty()) {
                        edtLabel.error = getString(R.string.input_something_error)
                        return
                    }

                    // We add the key to the set only if it's a new counter, not an existing one
                    if (!intent.hasExtra("counter_id"))
                        currentIDs_set.add(key_base)
                    // We store every value into the preferences file
                    prefs.edit().putString(key_base + "label", edtLabel.text.toString()).apply()
                    prefs.edit().putLong(key_base + "date", date.millis).apply()
                    prefs.edit().putInt(key_base + "color", selectedColor).apply()
                    prefs.edit().putInt(key_base + "color_index", selectedColor_index).apply()
                    prefs.edit().putStringSet("ids", currentIDs_set).apply()
                    prefs.edit().putBoolean(key_base + "isWidget", isWidget).apply()
                    prefs.edit().putBoolean(key_base + "notification", swtNotification.isChecked).apply()

                    Log.d("ConfigurationActivity", "Widget: $key_base, $label, $date, $selectedColor, $isWidget")

                    Log.d("ConfigurationActivity", "Added new counter with label" + edtLabel.text + ", ID " + key_base + " and date " + date.millis)

                    // If the counter is a widget, we start the WidgetUpdater, telling it to only update this widget
                    var updaterIntent: Intent
                    if (isWidget) {
                        updaterIntent = Intent(applicationContext, WidgetUpdater::class.java)
                        updaterIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        updaterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(Integer.parseInt(key_base)))
                        Log.d("UpdateReceiver", "Telling the WidgetUpdater to start")
                        applicationContext.sendBroadcast(updaterIntent)
                    }

                    // We tell the Notificator to start and update only this counter
                    updaterIntent = Intent(applicationContext, CounterNotificator::class.java)
                    updaterIntent.action = CounterNotificator.ACTION_UPDATE_NOTIFICATIONS
                    updaterIntent.putExtra((if (isWidget) "widget_ids" else "list_ids"), intArrayOf(Integer.parseInt(key_base)))
                    applicationContext.sendBroadcast(updaterIntent)

                    // We set this so the launcher knows that it has to add the widget to the screen (That is, that the user didn't cancel the operation)
                    val resultValue = Intent()
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)

                    setResult(Activity.RESULT_OK, resultValue)
                    finish()
                } catch (e: NumberFormatException) {
                    // This block is executed when the number the user wrote is too big
                    //(Or at least there's no other way of throwing a NumberFormatException here, I think)
                    edtDays.error = getString(R.string.number_too_big_error)
                }
            }
            R.id.btnCustomColor -> {
                // We create a new color picker and show it
                val cp = ColorPicker(this@ConfigurationActivity, 255, 255, 255)
                cp.show()

                cp.setCallback { color ->
                    // This callback is executed when the user presses "select". How the code here works is explained above, on line 132
                    var in_view: ColorImageView
                    for (j in 0..colorView.childCount - 2) {
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
            R.id.btnDate -> {
                // We show the date picker
                dateDialog.show()
            }
        }
    }

    // This method is executed when the user selects a date
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
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
        edtDays.setText(Math.abs(difference).toString())
    }

    override fun onResume() {
        super.onResume()
        Thread {
            Thread.sleep(500)
            runOnUiThread {
                spnType.errorColor = Color.parseColor("#888888")
                spnType.error = getString(R.string.until_or_since_desc) }
        }.start()
    }
}
