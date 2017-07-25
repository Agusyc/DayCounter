package com.agusyc.daycounter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView

import java.util.ArrayList
import java.util.HashSet

class MainActivity : AppCompatActivity() {

    private var prefs: SharedPreferences? = null

    private var lstWidgets: ArrayList<Widget>? = null

    private var adapter: WidgetListAdapter? = null

    private var lstWidgetsView: ListView? = null

    private var txtThereIsNothing1: TextView? = null
    private var txtThereIsNothing2: TextView? = null

    internal var dontAnimate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtThereIsNothing1 = findViewById(R.id.txtThere_Is_Nothing1) as TextView
        txtThereIsNothing2 = findViewById(R.id.txtThere_Is_Nothing2) as TextView

        prefs = getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE)

        lstWidgets = ArrayList<Widget>()

        adapter = WidgetListAdapter(this, lstWidgets as ArrayList<Widget>)

        // Attach the adapter to the listview
        lstWidgetsView = findViewById(R.id.lstWidgets) as ListView
        lstWidgetsView!!.adapter = adapter

        lstWidgetsView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val item = adapter!!.getItem(i)
            val configuration_intent = Intent(applicationContext, ConfigurationActivity::class.java)
            assert(item != null)
            configuration_intent.putExtra("widget_id", java.lang.Long.toString(item!!.id.toLong()))
            startActivity(configuration_intent)
        }

        updateListView()
    }

    override fun onResume() {
        super.onResume()
        dontAnimate = true
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
            startActivity(Intent(applicationContext, AboutActivity::class.java))
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    internal fun updateListView() {
        val widget_ids = prefs!!.getStringSet("ids", HashSet<String>())

        lstWidgets!!.clear()

        for (widget_id in widget_ids!!) {
            val alarm = Widget(this@MainActivity, Integer.parseInt(widget_id))
            lstWidgets!!.add(alarm)
            Log.d("MainActivity", "Parsed widget with ID " + widget_id)
        }

        if (lstWidgets!!.size == 0) {
            txtThereIsNothing1!!.visibility = View.VISIBLE
            txtThereIsNothing2!!.visibility = View.VISIBLE
            lstWidgetsView!!.visibility = View.INVISIBLE
        } else {
            txtThereIsNothing1!!.visibility = View.INVISIBLE
            txtThereIsNothing2!!.visibility = View.INVISIBLE
            lstWidgetsView!!.visibility = View.VISIBLE
        }

        adapter!!.notifyDataSetChanged()
    }

}
