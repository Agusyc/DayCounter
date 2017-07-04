package com.agusyc.daycounter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    private ArrayList<Widget> lstWidgets;

    private WidgetListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("DaysPrefs", MODE_PRIVATE);

        lstWidgets = new ArrayList<>();

        adapter = new WidgetListAdapter(this, lstWidgets);

        // Attach the adapter to the listview
        ListView listView = (ListView) findViewById(R.id.lstWidgets);
        listView.setAdapter(adapter);

        updateListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListView();
    }

    private void updateListView() {
        Set<String> widget_ids = prefs.getStringSet("ids", new HashSet<String>());

        lstWidgets.clear();

        for (String widget_id : widget_ids) {
            Widget alarm = new Widget(MainActivity.this, Long.parseLong(widget_id));
            lstWidgets.add(alarm);
            Log.d("MainActivity", "Parsed alarm with ID " + widget_id);
        }

        adapter.notifyDataSetChanged();
    }

}
