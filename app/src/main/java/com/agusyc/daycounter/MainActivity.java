package com.agusyc.daycounter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    private ArrayList<Widget> lstWidgets;

    private WidgetListAdapter adapter;

    private ListView lstWidgetsView;

    private TextView txtThereIsNothing1, txtThereIsNothing2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtThereIsNothing1 = (TextView) findViewById(R.id.txtThere_Is_Nothing1);
        txtThereIsNothing2 = (TextView) findViewById(R.id.txtThere_Is_Nothing2);

        prefs = getSharedPreferences("DaysPrefs", MODE_PRIVATE);

        lstWidgets = new ArrayList<>();

        adapter = new WidgetListAdapter(this, lstWidgets);

        // Attach the adapter to the listview
        lstWidgetsView = (ListView) findViewById(R.id.lstWidgets);
        lstWidgetsView.setAdapter(adapter);

        lstWidgetsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Widget item = adapter.getItem(i);
                Intent configuration_intent = new Intent(getApplicationContext(), ConfigurationActivity.class);
                assert item != null;
                configuration_intent.putExtra("widget_id", Long.toString(item.getID()));
                startActivity(configuration_intent);
            }
        });

        updateListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void updateListView() {
        Set<String> widget_ids = prefs.getStringSet("ids", new HashSet<String>());

        lstWidgets.clear();

        for (String widget_id : widget_ids) {
            Widget alarm = new Widget(MainActivity.this, Long.parseLong(widget_id));
            lstWidgets.add(alarm);
            Log.d("MainActivity", "Parsed widget with ID " + widget_id);
        }

        if (lstWidgets.size() == 0) {
            txtThereIsNothing1.setVisibility(View.VISIBLE);
            txtThereIsNothing2.setVisibility(View.VISIBLE);
            lstWidgetsView.setVisibility(View.INVISIBLE);
        } else {
            txtThereIsNothing1.setVisibility(View.INVISIBLE);
            txtThereIsNothing2.setVisibility(View.INVISIBLE);
            lstWidgetsView.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

}
