package com.agusyc.daycounter

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import java.util.*
import android.content.BroadcastReceiver

class MainActivity : AppCompatActivity() {
    private lateinit var widgetPrefs: SharedPreferences
    private lateinit var listPrefs: SharedPreferences
    private lateinit var settings: SharedPreferences
    private lateinit var lstCounters: ArrayList<Counter>
    private lateinit var adapter: CounterListAdapter
    private lateinit var lstCountersView: ListView
    private lateinit var txtThereIsNothing1: TextView
    private lateinit var txtThereIsNothing2: TextView
    private lateinit var lytMain: ConstraintLayout
    internal var dontAnimate = false
    private var dark_theme: Boolean = false
    private var animate_layout: Boolean = false

    // This is for receiving broadcasts for updating the list view
    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("MainActivity", "Updating list view")
            updateListView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        dark_theme = settings.getBoolean("dark_theme", false)
        if (dark_theme) setTheme(R.style.AppDarkTheme)
        setContentView(R.layout.activity_main)

        // We register the ListView receiver
        registerReceiver(broadcastReceiver, IntentFilter("com.agusyc.daycounter.UPDATE_LISTVIEW"))

        // We initialise all the views and objects
        findViewById(R.id.fab).setOnClickListener { _ ->
            val configuration_intent = Intent(applicationContext, ConfigurationActivity::class.java)
            configuration_intent.putExtra("isWidget", false)
            startActivity(configuration_intent)
        }
        txtThereIsNothing1 = findViewById(R.id.txtThere_Is_Nothing1) as TextView
        txtThereIsNothing2 = findViewById(R.id.txtThere_Is_Nothing2) as TextView
        widgetPrefs = getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)
        listPrefs = getSharedPreferences("ListDaysPrefs", Context.MODE_PRIVATE)
        lstCounters = ArrayList()
        adapter = CounterListAdapter(this, lstCounters)
        // We attach the adapter to the ListView
        lstCountersView = findViewById(R.id.lstCounters) as ListView
        lstCountersView.adapter = adapter

        // This is executed when a list item is clicked
        lstCountersView.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            // We open the configuration activity, sending it the counter data (Whether it's widget or not and the id)
            val item = adapter.getItem(i)
            val configuration_intent = Intent(applicationContext, ConfigurationActivity::class.java)
            configuration_intent.putExtra("counter_id", java.lang.Long.toString(item!!.id.toLong()))
            configuration_intent.putExtra("isWidget", item.isWidget)
            startActivity(configuration_intent)
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("animate_layout", false)) {
                // We go trough each object and animate it
                for (i in 0 until lytMain.childCount) {
                    val anim = ObjectAnimator.ofFloat(lytMain.getChildAt(i), "alpha", 0f, 1.0f)
                    anim.duration = 250
                    anim.start()
                }
                animate_layout = false
            }
        }

        // We update the ListView
        updateListView()
    }

    override fun onResume() {
        super.onResume()
        // When the activity is resumed, we don't want to run the animation
        dontAnimate = true
        // We update the list view (In case the user changed a counter)
        updateListView()
    }

    override fun onDestroy() {
        super.onDestroy()
        // We unregister the ListView receiver
        unregisterReceiver(broadcastReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.action_info -> {
                // We start the info activity
                startActivity(Intent(applicationContext, AboutActivity::class.java))
                return true
            }
            R.id.action_calculator -> {
                // We create and show the CalculatorDialog
                val cd: CalculatorDialog = if (dark_theme) CalculatorDialog(this, R.style.CalculatorDarkTheme) else CalculatorDialog(this, R.style.CalculatorTheme)
                cd.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                cd.show()
            }
            R.id.action_dark_theme -> {
                System.out.println("Setting the theme")
                // We get the current theme
                val dark_theme = settings.getBoolean("dark_theme", false)

                // We create, set up and play the animations
                val dark_background = Color.parseColor("#303030")
                val light_background = Color.parseColor("#fafafa")
                val colorAnimation: ValueAnimator
                colorAnimation = if (dark_theme) ValueAnimator.ofObject(ArgbEvaluator(), dark_background, light_background) else ValueAnimator.ofObject(ArgbEvaluator(), light_background, dark_background)
                colorAnimation.duration = 250
                colorAnimation.addUpdateListener { animator -> lytMain.setBackgroundColor(animator.animatedValue as Int) }
                // We go through each child and animate it
                for (i in 0 until lytMain.childCount) {
                    val anim = ObjectAnimator.ofFloat(lytMain.getChildAt(i), "alpha", 0f)
                    anim.duration = 250
                    anim.start()
                }
                colorAnimation.start()

                // We update the preferences file
                settings.edit().putBoolean("dark_theme", !dark_theme).apply()

                // We set this boolean, so the activity gets animated when we restart it
                animate_layout = true

                // We reload the activity after 250 milliseconds
                lytMain.postDelayed({ recreate() }, 250)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // This method takes care of updating the ListView
    internal fun updateListView() {
        // We get all the IDs
        val widgetCounterIds = widgetPrefs.getStringSet("ids", HashSet<String>())
        val listCounterIds = listPrefs.getStringSet("ids", HashSet<String>())

        // We clear the list
        lstCounters.clear()

        for (widget_id in widgetCounterIds!!) {
            Log.d("MainActivity", "Parsed widget counter with ID " + widget_id)
            // We parse each ID, make a counter object and add it to the list
            val counter = Counter(this@MainActivity, Integer.parseInt(widget_id), true)
            lstCounters.add(counter)
        }

        for (widget_id in listCounterIds!!) {
            Log.d("MainActivity", "Parsed list counter with ID " + widget_id)
            // We parse each ID, make a counter object and add it to the list
            val counter = Counter(this@MainActivity, Integer.parseInt(widget_id), false)
            lstCounters.add(counter)
        }

        // If after parsing and adding everything, the list's size is 0, we make the "txtThereIsNothing" views visible
        if (lstCounters.size == 0) {
            txtThereIsNothing1.visibility = View.VISIBLE
            txtThereIsNothing2.visibility = View.VISIBLE
            lstCountersView.visibility = View.INVISIBLE
        } else {
            txtThereIsNothing1.visibility = View.INVISIBLE
            txtThereIsNothing2.visibility = View.INVISIBLE
            lstCountersView.visibility = View.VISIBLE
        }

        // We trigger the adapter, so it actually updates the list
        adapter.notifyDataSetChanged()
    }

    public override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putBoolean("animate_layout", animate_layout)
    }

}
