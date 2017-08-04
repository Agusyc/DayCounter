package com.agusyc.daycounter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
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


class MainActivity : AppCompatActivity() {
    private var widgetPrefs: SharedPreferences? = null
    private var listPrefs: SharedPreferences? = null
    private var lstCounters: ArrayList<Counter>? = null
    private var adapter: CounterListAdapter? = null
    private var lstCountersView: ListView? = null
    private var txtThereIsNothing1: TextView? = null
    private var txtThereIsNothing2: TextView? = null
    internal var dontAnimate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

	// We initialise all the views and objects
        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { _ ->
            val configuration_intent = Intent(applicationContext, ConfigurationActivity::class.java)
            configuration_intent.putExtra("isWidget", false)
            startActivity(configuration_intent)
        }
        txtThereIsNothing1 = findViewById(R.id.txtThere_Is_Nothing1) as TextView
        txtThereIsNothing2 = findViewById(R.id.txtThere_Is_Nothing2) as TextView
        widgetPrefs = getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)
        listPrefs = getSharedPreferences("ListDaysPrefs", Context.MODE_PRIVATE)
        lstCounters = ArrayList<Counter>()
        adapter = CounterListAdapter(this, lstCounters as ArrayList<Counter>)
        // We attach the adapter to the listview
        lstCountersView = findViewById(R.id.lstCounters) as ListView
        lstCountersView!!.adapter = adapter

	// This is executed when a list item is clicked
        lstCountersView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            // We open the configuration activity, sending it the counter data (Wether it's widget or not and the id)
	    val item = adapter!!.getItem(i)
            val configuration_intent = Intent(applicationContext, ConfigurationActivity::class.java)
            configuration_intent.putExtra("counter_id", java.lang.Long.toString(item!!.id.toLong()))
            configuration_intent.putExtra("isWidget", item.isWidget)
            startActivity(configuration_intent)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_info) {
	    // We start the info activity
            startActivity(Intent(applicationContext, AboutActivity::class.java))
            return true
        } else if (id == R.id.action_calculator) {
            // We create and show the CalculatorDialog
	    val cd = CalculatorDialog(this, R.style.CalculatorDialog, fragmentManager)
            cd.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            cd.show()
        }

        return super.onOptionsItemSelected(item)
    }

    // This method takes care of updating the ListView
    internal fun updateListView() {
	// We get all the IDs
        val widgetCounterIds = widgetPrefs!!.getStringSet("ids", HashSet<String>())
        val listCounterIds = listPrefs!!.getStringSet("ids", HashSet<String>())

	// We clear the list
        lstCounters!!.clear()

        for (widget_id in widgetCounterIds!!) {
            Log.d("MainActivity", "Parsed widget counter with ID " + widget_id)
            // We parse each ID, make a counter object and add it to the list
	    val counter = Counter(this@MainActivity, Integer.parseInt(widget_id), true)
            lstCounters!!.add(counter)
        }

        for (widget_id in listCounterIds!!) {
            Log.d("MainActivity", "Parsed list counter with ID " + widget_id)
            // We parse each ID, make a counter object and add it to the list
            val counter = Counter(this@MainActivity, Integer.parseInt(widget_id), false)
            lstCounters!!.add(counter)
        }

	// If after parsing and adding everything, the list's size is 0, we make the "txtThereIsNothing" views visible
        if (lstCounters!!.size == 0) {
            txtThereIsNothing1!!.visibility = View.VISIBLE
            txtThereIsNothing2!!.visibility = View.VISIBLE
            lstCountersView!!.visibility = View.INVISIBLE
        } else {
            txtThereIsNothing1!!.visibility = View.INVISIBLE
            txtThereIsNothing2!!.visibility = View.INVISIBLE
            lstCountersView!!.visibility = View.VISIBLE
        }

	// We trigger the adapter, so it actually updates the list
        adapter!!.notifyDataSetChanged()
    }
}
